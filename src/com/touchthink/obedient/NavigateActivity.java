package com.touchthink.obedient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class NavigateActivity extends Activity implements OnViewChangeListener,OnInstallFileChangeListener 
{
	
	private NavigateScrollLayout mScrollLayout;
	private ImageView[] imgs;
	private int count;
	private int currentItem;
	private Button startBtn;
	private RelativeLayout mainRLayout;
	private LinearLayout pointLLayout;
	private LinearLayout leftLayout;
	private LinearLayout rightLayout;
	private LinearLayout animLayout;
	
	private CopyDataFileTask install_data_file;
	private Thread install_file_thread;
	
	
	
	private int status;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigate);
        initView();
    }
    
	private void initView() {
		
		status = 0;
		install_data_file = new CopyDataFileTask(this);
		install_file_thread = new Thread(install_data_file);

		mScrollLayout  = (NavigateScrollLayout) findViewById(R.id.ScrollLayout);
		pointLLayout = (LinearLayout) findViewById(R.id.llayout);
		mainRLayout = (RelativeLayout) findViewById(R.id.mainRLayout);
		startBtn = (Button) findViewById(R.id.startBtn);
		startBtn.setOnClickListener(onClick);
		animLayout = (LinearLayout) findViewById(R.id.animLayout);
		leftLayout  = (LinearLayout) findViewById(R.id.leftLayout);
		rightLayout  = (LinearLayout) findViewById(R.id.rightLayout);
		count = mScrollLayout.getChildCount();
		imgs = new ImageView[count];
		for(int i = 0; i< count;i++) {
			imgs[i] = (ImageView) pointLLayout.getChildAt(i);
			imgs[i].setEnabled(true);
			imgs[i].setTag(i);
		}
		currentItem = 0;
		imgs[currentItem].setEnabled(false);
		mScrollLayout.SetOnViewChangeListener(this);
		
		install_file_thread.start();
		
		
	}
	
	private View.OnClickListener onClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.startBtn:
				int count = 0;
//				while(true){
//					if(status == 0 && count < 10){
//						try {
//							Thread.sleep(1000);
//							count ++;
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					} else {
//						break;
//						
//					}
//				}

				
				mScrollLayout.setVisibility(View.GONE);
				pointLLayout.setVisibility(View.GONE);
				animLayout.setVisibility(View.VISIBLE);
				mainRLayout.setBackgroundResource(R.drawable.whatsnew_bg);
				


				//启动开门动画
				Animation leftOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.translate_left);
				Animation rightOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.translate_right);
				
				leftLayout.setAnimation(leftOutAnimation);
				rightLayout.setAnimation(rightOutAnimation);
				
				//打开新窗口
				leftOutAnimation.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
//						mainRLayout.setBackgroundColor(0);
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) {
					}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						leftLayout.setVisibility(View.GONE);
						rightLayout.setVisibility(View.GONE);
						//打开主窗口
						try{

							Intent intent = new Intent(NavigateActivity.this, ObedientActivity.class);
							startActivity(intent);
							finish();
						
						}catch(Exception ex){
							ex.printStackTrace();
						}
						
						overridePendingTransition(R.anim.zoom_out_enter, R.anim.zoom_out_exit);
					}
				});
				
				break;
			}
		}
	};

	@Override
	public void OnViewChange(int position) {
		setcurrentPoint(position);
	}

	private void setcurrentPoint(int position) {
		if(position < 0 || position > count -1 || currentItem == position) {
			return;
		}
		imgs[currentItem].setEnabled(true);
		imgs[position].setEnabled(false);
		currentItem = position;
	}

	@Override
	public void OnStatusChange(int message) {
		
		status = message;
		
	}

	@Override
	public void OnError(int error) {
		
	}
}