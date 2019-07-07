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
	private Thread seekBarThread;//进度条线程
	private String musicData;//歌曲路径
	private Thread playerThread;//播放歌曲线程
	@Override
	public void onCreate() {//初始化
		// TODO Auto-generated method stub
		super.onCreate();
		musicData="";
		mbinder = new MyBinder();
		mediaPlayer = new MediaPlayer();
	}
	
	public void setData(String musicData){//设置歌曲路径
		this.musicData = musicData;
	}
	
	public String getData(){//获取歌曲路径
		return musicData;
	}
	
	public void prepare(){//播放前的准备
		try { 
			mediaPlayer.reset();//重置mediaPlayer对象
			mediaPlayer.setDataSource(musicData);//设置播放歌曲的路径
			seekBarThread = new Thread(backgroudSeek);//实例化进度条线程
			playerThread = new Thread(player);//实例化播放线程
			mediaPlayer.prepareAsync();	//使用异步准备模式，准备好媒体资源后将调用onPrepared(MediaPlayer mp)方法
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
		//设置媒体资源准备完成监听
		mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			//媒体资源准备完成调用，启动进度条线程，和播放线程
			public void onPrepared(MediaPlayer mp) {
				// TODO Auto-generated method stub
				if(!seekBarThread.isAlive()&&!playerThread.isAlive()){
					seekBarThread.start();
					playerThread.start();		
				}	
			}
		});
		//设置歌曲播放完成监听
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			//播放完成调用，发送广播通知Activity，切换下一首歌
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				Intent intent = new Intent("com.example.myMusic.MUSICAUTO_ACTION");
				sendBroadcast(intent);
			}
		});
		//设置播放错误监听
	    mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				// TODO Auto-generated method stub
				return true;
			}
		});
	}

	//实现播放/暂停
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
   //停止播放
	public void stop(){
		seekBarThread.interrupt();
		playerThread.interrupt();
		mediaPlayer.stop();
	}
	//判断歌曲是否在播放
	public boolean isPlaying(){
		return mediaPlayer.isPlaying();
	}
	//获取当前播放时刻
	public int getCurrenPosition(){
		return mediaPlayer.getCurrentPosition();
	}
	//将歌曲跳转到指定时刻播放
	public void seekTo(int time){
		mediaPlayer.seekTo(time);
	}
	
	private Runnable backgroudSeek = new Runnable(){//实现Runnable接口,并重写run()方法
		@Override
		public void run() {
			// TODO Auto-generated method stub
				try {
					while(!Thread.interrupted()){
						//调用Activity中的UpdataGUI方法刷新屏幕
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
			mediaPlayer.start();//播放音乐
		}
	};
	//Activity销毁时调用，停止播放，释放资源，停止两个子线程
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
	//自定义一个MyBinder类继承Binder，定义一个getService()方法返回MusicService对象的引用
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
