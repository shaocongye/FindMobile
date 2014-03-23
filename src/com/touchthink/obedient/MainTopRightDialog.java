package com.touchthink.obedient;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainTopRightDialog extends Activity {
	//private MyDialog dialog;
	private LinearLayout layout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_top_right_dialog);		
	}

	//退出窗口
	public void exitmain(View view) {
		Intent intent = new Intent (MainTopRightDialog.this,ExitFromSettings.class);			
		startActivity(intent);
		finish();
    }
	
	//去web
	public void toweb(View view) {
    }
	//设置
	public void setup(View view) {
    }
	
	//监听
	public void startrun(View view) {
    }
	
	//关闭菜单窗口
	@Override
	public boolean onTouchEvent(MotionEvent event){
		finish();
		return true;
	}
}
