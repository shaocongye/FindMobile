package com.touchthink.obedient;



import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.WindowManager;

public class Appstart extends Activity{
	private static String PREFEREBCE_NAME = "Obedient";
	private static String FIRSTRUN_PROPERTY = "firstrun";
	private SharedPreferences preferences;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.appstart);

		Preference();
		
//		new Handler().postDelayed(new Runnable(){
//			@Override
//			public void run(){
//				Intent intent = new Intent (Appstart.this,Welcome.class);			
//				startActivity(intent);			
//				Appstart.this.finish();
//			}
//		}, 1000);
		
		
   }
	
	
	
	private void Preference()
	{
		this.preferences = getSharedPreferences(PREFEREBCE_NAME,MODE_WORLD_READABLE);
		if(this.preferences.getBoolean(FIRSTRUN_PROPERTY, true))
		{
			Intent intent = new Intent(Appstart.this,NavigateActivity.class);
			startActivity(intent);
			finish();
			
			Editor editor = this.preferences.edit();
			editor.putBoolean("firstrun", false);
			editor.commit();
		}else {
			
			new Handler().postDelayed(new Runnable(){
				@Override
				public void run(){
					Intent intent = new Intent (Appstart.this,MainActivity.class);			
					startActivity(intent);			
					Appstart.this.finish();
				}
			}, 1000);		
		}

	}

	
}