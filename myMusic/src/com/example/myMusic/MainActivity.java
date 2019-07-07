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
	private int _id ;   //歌曲id                   
	private String _title;      //歌曲标题            
	private String _musician ;  //艺术家
	private String _article;    //专辑
	private String _datapath;  //歌曲路径
	private long _size;       //歌曲大小
	private int _duration;    //歌曲时长
	private ListView listView;
	private ContentResolver resolver; 
	private Button previous;
	private Button play;
	private Button next;
	private TextView musicInfo;//显示歌曲标题
	private TextView musicrainInfo;//显示艺术家
	private static SeekBar seekBar;//进度条
	private static TextView playTime;//显示歌曲当前时间
	private TextView endTime;//显示歌曲总时长
	private static  int time;//保存歌曲当前时间
	private ServiceConnection mConnection;
	private MusicService mService;
	private boolean isPlay = false;//判断歌曲是否在播放
	private static int positions = 0;//保存正在播放的歌曲的位置
	private final int ORDER = 1;//顺序播放
	private final int RANDOM = 2;//随机播放
	//private final int CYCLE = 3;
	private final int SINGLE = 3;//单曲循环
	private int mode = 1;//歌曲播放模式
	private Cursor cursor;
	private Intent intent;
	private MediaResource[] mediares;
	private static Handler handler;
	private  ActivityMediaAutoPlay mReceiver;
	@Override
	protected void onCreate(Bundle savedInstanceState) {//onCreate()初始化以上字段
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
		resolver = this.getContentResolver();//获取ContentResolver对象
		//从Android媒体数据中查询所有歌曲的信息，并按照其ID排序，返回一个Cursor对象
	    cursor=resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,null,MediaStore.Audio.Media._ID);
		String[] list = new String[cursor.getCount()];//定义一个list数组保存歌曲的_title;
		mediares = new MediaResource[cursor.getCount()];//定义一个mediares数组保存每一首歌
		if(cursor.moveToFirst()){
			for(int i = 0;i < cursor.getCount();i++){//此循环用于将cursor对象中的歌曲信息提取出来，并用相应字段保存
				_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
				_title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
				_musician = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
				_article = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
				_datapath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
				_size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
				_duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
				list[i] = _title;
				//利用以上字段创建MediaResource对象
				mediares[i] = new MediaResource( _id, _title, _musician, _article, _datapath, _size ,_duration);
				cursor.moveToNext();
			}
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
		listView.setAdapter(adapter);//将歌曲_title显示在ListView中

		//获取Service对象
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
		bindService(intent,mConnection,Context.BIND_AUTO_CREATE);//以绑定方式启动Service，并获取Service对象
		//mediaPlayer = mService.getmediaPlayer();
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){//设置ListView监听
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				// TODO Auto-generated method stub
				play(mediares[position].getDatapath(),position);//根据position播放相应歌曲
				play.setText("暂停");
				// Toast.makeText(MainActivity.this, intent.getDataString(), Toast.LENGTH_SHORT).show();
				positions = position;
				}
			});
	
		//上一曲
		previous.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				previous(mode);//调用播放上一曲的方法
			}
		});
		//播放或暂停
		play.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mService.getData()==""){
					play.setText("暂停");
					play(mediares[positions].getDatapath(),positions);
				}else{
					if(!isPlay){
						mService.play();
						time = mService.getCurrenPosition();
						play.setText("播放");
						isPlay = true;
					}else{
						mService.play();
						mService.seekTo(time);
						play.setText("暂停");
						isPlay = false;
					}
				}
			}
		});
		//下一曲
		next.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				next(mode);//调用播放下一曲的方法
			}
		});
		//设置进度条监听
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {//此方法会在拖动进度条时调用
				// TODO Auto-generated method stub
				if(mService.getData()!=""){//若歌曲的路径存在则执行
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

	public void next(int mode){//播放下一曲的方法
		switch(mode){//根据当前的播放模式切换下一首
		case ORDER:if(positions==mediares.length-1)positions=0;else positions++;play(mediares[positions].getDatapath(),positions);break;	
		case RANDOM:int random = new Random().nextInt(mediares.length);positions=positions==random ? ++positions : random;
		            play(mediares[positions].getDatapath(),positions);break;
		case SINGLE:play(mediares[positions].getDatapath(),positions);
		}
	}
	public void previous(int mode){//播放上一首歌的方法
		switch(mode){//同上
		case ORDER:if(positions==0)positions=mediares.length-1;else positions--;play(mediares[positions].getDatapath(),positions);break;
		case RANDOM:int random = new Random().nextInt(mediares.length);positions=positions==random ? ++positions : random;
		            play(mediares[positions].getDatapath(),positions);break;
		case SINGLE:play(mediares[positions].getDatapath(),positions);
		}
	}

	 //播放模块
	public void play(String musicData,int position){
		if(mService.getData()!=""){//若当前有歌曲在播放，则停止，并重置进度条和播放时间
			mService.stop();
			time = 0;
			seekBar.setProgress(time);
			playTime.setText(showTime(time));	
		}
		mService.setData(musicData);//设置播放路径
		mService.prepare();//准备播放
		musicInfo.setText(mediares[position].getTitle());//设置音乐标题
		musicrainInfo.setText(mediares[position].getMusician());//设置艺术家
		endTime.setText(showTime(mediares[position].getDuration()));//设置歌曲时长
		seekBar.setMax(mediares[position].getDuration());//设置进度条的长度 
	}

	public static void UpdateGUI(int refresh){//用于刷新屏幕，此方法在MusicService中调用
		   time = refresh;
		   handler.post(refreshSeekBar);
	}
	
	private static Runnable refreshSeekBar = new Runnable(){

		@Override
		public void run() {//刷新进度条和播放时间
			// TODO Auto-generated method stub
			playTime.setText(showTime(time));
			seekBar.setProgress(time);
		}
		
	};
	
	public static String showTime(int time){//格式化输出时间
		time/=1000;
		return String.format("%02d:%02d", time/60,time%60);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {//播放模式菜单
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.item1:mode=ORDER;Toast.makeText(this, "顺序播放", Toast.LENGTH_SHORT).show();break;
		case R.id.item2:mode=RANDOM;Toast.makeText(this, "随机播放", Toast.LENGTH_SHORT).show();break;
		//case R.id.item3:mode=CYCLE;break;
		case R.id.item4:mode=SINGLE;Toast.makeText(this, "单曲循环", Toast.LENGTH_SHORT).show();break;
		case R.id.item5:finish();break;
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean onCreateOptionsMenu(Menu menu){
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	protected void onDestroy() {//Activity销毁前调用，停止服务，注销广播
		// TODO Auto-generated method stub
		unbindService(mConnection);
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}
	//设置一个广播接受器，接受歌曲播放完成的广播，广播在MusicService中发送
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

