package com.example.myMusic;

import java.util.Random;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private int _id ;   //����id                   
	private String _title;      //��������            
	private String _musician ;  //������
	private String _article;    //ר��
	private String _datapath;  //����·��
	private long _size;       //������С
	private int _duration;    //����ʱ��
	private ListView listView;
	private ContentResolver resolver; 
	private Button previous;
	private Button play;
	private Button next;
	private TextView musicInfo;//��ʾ��������
	private TextView musicrainInfo;//��ʾ������
	private static SeekBar seekBar;//������
	private static TextView playTime;//��ʾ������ǰʱ��
	private TextView endTime;//��ʾ������ʱ��
	private static  int time;//���������ǰʱ��
	private ServiceConnection mConnection;
	private MusicService mService;
	private boolean isPlay = false;//�жϸ����Ƿ��ڲ���
	private static int positions = 0;//�������ڲ��ŵĸ�����λ��
	private final int ORDER = 1;//˳�򲥷�
	private final int RANDOM = 2;//�������
	//private final int CYCLE = 3;
	private final int SINGLE = 3;//����ѭ��
	private int mode = 1;//��������ģʽ
	private Cursor cursor;
	private Intent intent;
	private MediaResource[] mediares;
	private static Handler handler;
	private  ActivityMediaAutoPlay mReceiver;
	@Override
	protected void onCreate(Bundle savedInstanceState) {//onCreate()��ʼ�������ֶ�
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		IntentFilter filter = new IntentFilter();
		mReceiver = new ActivityMediaAutoPlay();
		filter.addAction("com.example.myMusic.MUSICAUTO_ACTION");
		registerReceiver(mReceiver,filter);
		previous = (Button)findViewById(R.id.button1);
		play = (Button)findViewById(R.id.button2);
		next = (Button)findViewById(R.id.button3);
		musicInfo = (TextView)findViewById(R.id.textView1);
		musicrainInfo = (TextView)findViewById(R.id.textView4);
		playTime = (TextView)findViewById(R.id.textView2);
		endTime = (TextView)findViewById(R.id.textView3);
		seekBar = (SeekBar)findViewById(R.id.seekBar1);
		listView = (ListView)findViewById(R.id.listView1);
		handler = new Handler();
		resolver = this.getContentResolver();//��ȡContentResolver����
		//��Androidý�������в�ѯ���и�������Ϣ����������ID���򣬷���һ��Cursor����
	    cursor=resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,null,MediaStore.Audio.Media._ID);
		String[] list = new String[cursor.getCount()];//����һ��list���鱣�������_title;
		mediares = new MediaResource[cursor.getCount()];//����һ��mediares���鱣��ÿһ�׸�
		if(cursor.moveToFirst()){
			for(int i = 0;i < cursor.getCount();i++){//��ѭ�����ڽ�cursor�����еĸ�����Ϣ��ȡ������������Ӧ�ֶα���
				_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
				_title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
				_musician = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
				_article = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
				_datapath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
				_size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
				_duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
				list[i] = _title;
				//���������ֶδ���MediaResource����
				mediares[i] = new MediaResource( _id, _title, _musician, _article, _datapath, _size ,_duration);
				cursor.moveToNext();
			}
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
		listView.setAdapter(adapter);//������_title��ʾ��ListView��

		//��ȡService����
		mConnection = new ServiceConnection(){
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				// TODO Auto-generated method stub
				mService = ((MusicService.MyBinder)service).getService();
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				mService = null;
			}
		};
		intent = new Intent(this,MusicService.class);
		//startService(intent);
		bindService(intent,mConnection,Context.BIND_AUTO_CREATE);//�԰󶨷�ʽ����Service������ȡService����
		//mediaPlayer = mService.getmediaPlayer();
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){//����ListView����
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				// TODO Auto-generated method stub
				play(mediares[position].getDatapath(),position);//����position������Ӧ����
				play.setText("��ͣ");
				// Toast.makeText(MainActivity.this, intent.getDataString(), Toast.LENGTH_SHORT).show();
				positions = position;
				}
			});
	
		//��һ��
		previous.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				previous(mode);//���ò�����һ���ķ���
			}
		});
		//���Ż���ͣ
		play.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mService.getData()==""){
					play.setText("��ͣ");
					play(mediares[positions].getDatapath(),positions);
				}else{
					if(!isPlay){
						mService.play();
						time = mService.getCurrenPosition();
						play.setText("����");
						isPlay = true;
					}else{
						mService.play();
						mService.seekTo(time);
						play.setText("��ͣ");
						isPlay = false;
					}
				}
			}
		});
		//��һ��
		next.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				next(mode);//���ò�����һ���ķ���
			}
		});
		//���ý���������
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {//�˷��������϶�������ʱ����
				// TODO Auto-generated method stub
				if(mService.getData()!=""){//��������·��������ִ��
					playTime.setText(showTime(seekBar.getProgress()));
					mService.seekTo(seekBar.getProgress());
				}
				 //isChanging = false;
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				  //isChanging = true;
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				//playTime.setText(mService.showTime(progress));
			}
		});	
	}

	public void next(int mode){//������һ���ķ���
		switch(mode){//���ݵ�ǰ�Ĳ���ģʽ�л���һ��
		case ORDER:if(positions==mediares.length-1)positions=0;else positions++;play(mediares[positions].getDatapath(),positions);break;	
		case RANDOM:int random = new Random().nextInt(mediares.length);positions=positions==random ? ++positions : random;
		            play(mediares[positions].getDatapath(),positions);break;
		case SINGLE:play(mediares[positions].getDatapath(),positions);
		}
	}
	public void previous(int mode){//������һ�׸�ķ���
		switch(mode){//ͬ��
		case ORDER:if(positions==0)positions=mediares.length-1;else positions--;play(mediares[positions].getDatapath(),positions);break;
		case RANDOM:int random = new Random().nextInt(mediares.length);positions=positions==random ? ++positions : random;
		            play(mediares[positions].getDatapath(),positions);break;
		case SINGLE:play(mediares[positions].getDatapath(),positions);
		}
	}

	 //����ģ��
	public void play(String musicData,int position){
		if(mService.getData()!=""){//����ǰ�и����ڲ��ţ���ֹͣ�������ý������Ͳ���ʱ��
			mService.stop();
			time = 0;
			seekBar.setProgress(time);
			playTime.setText(showTime(time));	
		}
		mService.setData(musicData);//���ò���·��
		mService.prepare();//׼������
		musicInfo.setText(mediares[position].getTitle());//�������ֱ���
		musicrainInfo.setText(mediares[position].getMusician());//����������
		endTime.setText(showTime(mediares[position].getDuration()));//���ø���ʱ��
		seekBar.setMax(mediares[position].getDuration());//���ý������ĳ��� 
	}

	public static void UpdateGUI(int refresh){//����ˢ����Ļ���˷�����MusicService�е���
		   time = refresh;
		   handler.post(refreshSeekBar);
	}
	
	private static Runnable refreshSeekBar = new Runnable(){

		@Override
		public void run() {//ˢ�½������Ͳ���ʱ��
			// TODO Auto-generated method stub
			playTime.setText(showTime(time));
			seekBar.setProgress(time);
		}
		
	};
	
	public static String showTime(int time){//��ʽ�����ʱ��
		time/=1000;
		return String.format("%02d:%02d", time/60,time%60);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {//����ģʽ�˵�
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.item1:mode=ORDER;Toast.makeText(this, "˳�򲥷�", Toast.LENGTH_SHORT).show();break;
		case R.id.item2:mode=RANDOM;Toast.makeText(this, "�������", Toast.LENGTH_SHORT).show();break;
		//case R.id.item3:mode=CYCLE;break;
		case R.id.item4:mode=SINGLE;Toast.makeText(this, "����ѭ��", Toast.LENGTH_SHORT).show();break;
		case R.id.item5:finish();break;
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean onCreateOptionsMenu(Menu menu){
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	protected void onDestroy() {//Activity����ǰ���ã�ֹͣ����ע���㲥
		// TODO Auto-generated method stub
		unbindService(mConnection);
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}
	//����һ���㲥�����������ܸ���������ɵĹ㲥���㲥��MusicService�з���
	public class ActivityMediaAutoPlay extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			next(mode);
		}
	}
	/*
	public class ActivitySeekBar extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			
		}
		
	}
	*/
}

