package com.example.myMusic;

import java.io.IOException;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

public class MusicService extends Service {

	private IBinder mbinder;
	private MediaPlayer mediaPlayer;
	private Thread seekBarThread;//�������߳�
	private String musicData;//����·��
	private Thread playerThread;//���Ÿ����߳�
	@Override
	public void onCreate() {//��ʼ��
		// TODO Auto-generated method stub
		super.onCreate();
		musicData="";
		mbinder = new MyBinder();
		mediaPlayer = new MediaPlayer();
	}
	
	public void setData(String musicData){//���ø���·��
		this.musicData = musicData;
	}
	
	public String getData(){//��ȡ����·��
		return musicData;
	}
	
	public void prepare(){//����ǰ��׼��
		try { 
			mediaPlayer.reset();//����mediaPlayer����
			mediaPlayer.setDataSource(musicData);//���ò��Ÿ�����·��
			seekBarThread = new Thread(backgroudSeek);//ʵ�����������߳�
			playerThread = new Thread(player);//ʵ���������߳�
			mediaPlayer.prepareAsync();	//ʹ���첽׼��ģʽ��׼����ý����Դ�󽫵���onPrepared(MediaPlayer mp)����
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//����ý����Դ׼����ɼ���
		mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			//ý����Դ׼����ɵ��ã������������̣߳��Ͳ����߳�
			public void onPrepared(MediaPlayer mp) {
				// TODO Auto-generated method stub
				if(!seekBarThread.isAlive()&&!playerThread.isAlive()){
					seekBarThread.start();
					playerThread.start();		
				}	
			}
		});
		//���ø���������ɼ���
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			//������ɵ��ã����͹㲥֪ͨActivity���л���һ�׸�
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				Intent intent = new Intent("com.example.myMusic.MUSICAUTO_ACTION");
				sendBroadcast(intent);
			}
		});
		//���ò��Ŵ������
	    mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				// TODO Auto-generated method stub
				return true;
			}
		});
	}

	//ʵ�ֲ���/��ͣ
   public void play(){
		if(mediaPlayer.isPlaying()){
			mediaPlayer.pause();
		}else{
			mediaPlayer.start();
		}
	}
   /*
	public void previous(){
		if(mediaPlayer.isPlaying()){
			mediaPlayer.stop();
			mediaPlayer.release();
			prepare();
			mediaPlayer.start();
		}else{
			prepare();
			mediaPlayer.start();
		}
	}
	
	public void next(){
		if(mediaPlayer.isPlaying()){
			mediaPlayer.stop();
			mediaPlayer.release();
			prepare();
			mediaPlayer.start();
		}else{
			prepare();
			mediaPlayer.start();
		}
	}
	*/
   //ֹͣ����
	public void stop(){
		seekBarThread.interrupt();
		playerThread.interrupt();
		mediaPlayer.stop();
	}
	//�жϸ����Ƿ��ڲ���
	public boolean isPlaying(){
		return mediaPlayer.isPlaying();
	}
	//��ȡ��ǰ����ʱ��
	public int getCurrenPosition(){
		return mediaPlayer.getCurrentPosition();
	}
	//��������ת��ָ��ʱ�̲���
	public void seekTo(int time){
		mediaPlayer.seekTo(time);
	}
	
	private Runnable backgroudSeek = new Runnable(){//ʵ��Runnable�ӿ�,����дrun()����
		@Override
		public void run() {
			// TODO Auto-generated method stub
				try {
					while(!Thread.interrupted()){
						//����Activity�е�UpdataGUI����ˢ����Ļ
						MainActivity.UpdateGUI(mediaPlayer.getCurrentPosition());
					    Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
	};
   
	private Runnable player = new Runnable(){
		@Override
		public void run() {
			// TODO Auto-generated method stub
			mediaPlayer.start();//��������
		}
	};
	//Activity����ʱ���ã�ֹͣ���ţ��ͷ���Դ��ֹͣ�������߳�
   @Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		mediaPlayer.stop();
		mediaPlayer.release();
	    playerThread.interrupt();
		seekBarThread.interrupt();
		return super.onUnbind(intent);	
	}

	@Override
	public void onRebind(Intent intent) {
		// TODO Auto-generated method stub
		super.onRebind(intent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mbinder;
	}
	//�Զ���һ��MyBinder��̳�Binder������һ��getService()��������MusicService���������
	public class MyBinder extends Binder{
		public MusicService getService(){
			return MusicService.this;
		}
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
}
