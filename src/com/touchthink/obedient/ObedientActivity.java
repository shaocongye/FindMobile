package com.touchthink.obedient;



import com.touchthink.obedient.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

public class ObedientActivity extends Activity implements OnTouchListener, RecognitionListener {

	static {
		System.loadLibrary("pocketsphinx_jni");
	}
	
	
	AudioMonitorTask audio;
	

	  
	Thread audio_thread;

	TextView edit_title;
	TextView edit_text;
	ImageButton button_start;
	
	String text_str;
	
	
	int status;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		status = 0;
		
		
		
		this.edit_title = (TextView) findViewById(R.id.tvTitle);
		this.edit_title.setTextColor(Color.WHITE);

		this.edit_text = (TextView) findViewById(R.id.tvOutText);
		this.edit_text.setTextColor(Color.BLACK);
		
		this.button_start = (ImageButton) findViewById(R.id.voiceRecored);

		button_start.setOnTouchListener(this);	
		
		audio = new AudioMonitorTask(this);
		audio_thread = new Thread(audio);
		
		text_str = "";

		
	}
//
//	public void onCreate(Bundle savedInstanceState)
//	{
//		super.onCreate(savedInstanceState);
//		//requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
//		setContentView(R.layout.main);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN); //全屏显示   
//		status = 0;
//        preferences = getSharedPreferences("Obedient",MODE_WORLD_READABLE); 
//		//判断是不是首次登录，
//		if (preferences.getBoolean("firststart", true)) {
//			//首先启动复制数据的线程，
//			//第一次运行，调用导航界面
//			try{
//		        Intent intent = new Intent(ObedientActivity.this, WelcomActivity.class);
//		        startActivity(intent);
//				finish();
//			}catch(Exception ex){
//				ex.printStackTrace();
//			}
//
//
//			editor = preferences.edit();
//		    //将登录标志位设置为false，下次登录时不在显示首次登录界面
//		    editor.putBoolean("firststart", false);
//		    editor.commit();
//		    
//		    //启动导航界面
//		    //Intent intent = new Intent("com.yutao.business.GUIDE");
//		    //startActivity(intent);
//		    //finish();
//		}
//		   
//		//login();
//		this.edit_title = (TextView) findViewById(R.id.tvTitle);
//		this.edit_title.setTextColor(Color.WHITE);
//
//		this.edit_text = (TextView) findViewById(R.id.tvOutText);
//		this.edit_text.setTextColor(Color.BLACK);
//		
//		this.button_start = (ImageButton) findViewById(R.id.voiceRecored);
//
//		button_start.setOnTouchListener(this);	
//		
//		audio = new AudioMonitorTask(this);
//		audio_thread = new Thread(audio);
//		
//		text_str = "";
//		
//	}
//
	
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			
			if(status == 0){
				if(audio == null)
					audio = new AudioMonitorTask(this);
			
				if(audio_thread == null)
					audio_thread = new Thread(audio);
			
				audio_thread.start();
				
				status = 1;
			} else {
				audio.stop();
				try {
					audio_thread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				audio = null;
				audio_thread = null;
				status = 0;
			}
			
			break;
		case MotionEvent.ACTION_UP:
			
			break;
		default:
			;
		}
		
		return false;
	}

	@Override
	public void onPartialResults(Bundle b) {
		
	}

	@Override
	public void onResults(Bundle b) {
		Log.d(getClass().getName(), "");
		
		if(audio != null)
		{
			audio.play(b.getString("hyp"));
		}
		
	}

	@Override
	public void onError(int err) {
		
	}



	
}
