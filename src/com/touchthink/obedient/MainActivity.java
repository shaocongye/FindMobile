package com.touchthink.obedient;


import java.util.ArrayList;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public static MainActivity instance = null;
	 
	private ViewPager mTabPager;	
	private ImageView mTabImg;// 动画图片
	private ImageView mTab1,mTab4;   //mTab2,mTab3,
	
	private int zero = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	
	private int one;//单个水平动画位移
	private int two;
	private int three;
	
	private LinearLayout mClose;
    private LinearLayout mCloseBtn;
    private View layout;	
	private boolean menu_display = false;
	private PopupWindow menuWindow;
	private LayoutInflater inflater;
	//private Button mRightBtn;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_form);
         
        //启动activity时不自动弹出软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
        instance = this;
        
        //设置按钮
        mTabPager = (ViewPager)findViewById(R.id.tabpager);
        mTabPager.setOnPageChangeListener(new MyOnPageChangeListener());
        
        mTab1 = (ImageView) findViewById(R.id.img_weixin);
        mTab4 = (ImageView) findViewById(R.id.img_settings);
        
        mTabImg = (ImageView) findViewById(R.id.img_tab_now);
        mTab1.setOnClickListener(new MyOnClickListener(0));
        mTab4.setOnClickListener(new MyOnClickListener(3));
        
        Display currDisplay = getWindowManager().getDefaultDisplay();//获取屏幕当前分辨率
        int displayWidth = currDisplay.getWidth();
        int displayHeight = currDisplay.getHeight();
        one = displayWidth/4; //设置水平动画平移大小
        two = one*2;
        three = one*3;
        //Log.i("info", "获取的屏幕分辨率为" + one + two + three + "X" + displayHeight);
        
        //InitImageView();//使用动画
        //将要分页显示的View装入数组中
        LayoutInflater mLi = LayoutInflater.from(this);
        View view1 = mLi.inflate(R.layout.main_tab_weixin, null);
        View view4 = mLi.inflate(R.layout.main_tab_settings, null);
        
        //每个页面的view数据
        final ArrayList<View> views = new ArrayList<View>();
        views.add(view1);
        views.add(view4);
        
        //填充ViewPager的数据适配器
        PagerAdapter mPagerAdapter = new PagerAdapter() {
			
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}
			
			@Override
			public int getCount() {
				return views.size();
			}

			@Override
			public void destroyItem(View container, int position, Object object) {
				((ViewPager)container).removeView(views.get(position));
			}
					
			@Override
			public Object instantiateItem(View container, int position) {
				((ViewPager)container).addView(views.get(position));
				return views.get(position);
			}
		};
		
		mTabPager.setAdapter(mPagerAdapter);
    }
    
    /**
	 * 头标点击监听
	 */
	public class MyOnClickListener implements View.OnClickListener {
		private int index = 1;

		public MyOnClickListener(int i) {
			index = i;
		}
		
		@Override
		public void onClick(View v) {
			mTabPager.setCurrentItem(index);
		}
	};
    
	 /* 页卡切换监听
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			Animation animation = null;
			switch (arg0) {
			case 0:
				mTab1.setImageDrawable(getResources().getDrawable(R.drawable.tab_weixin_pressed));
				animation = new TranslateAnimation(three, 0, 0, 0);
				mTab4.setImageDrawable(getResources().getDrawable(R.drawable.tab_settings_normal));
				break;
			case 1:
				mTab4.setImageDrawable(getResources().getDrawable(R.drawable.tab_settings_pressed));
					
				animation = new TranslateAnimation(zero, three, 0, 0);
				mTab1.setImageDrawable(getResources().getDrawable(R.drawable.tab_weixin_normal));

				break;	
			}
			currIndex = arg0;
			animation.setFillAfter(true);// True:图片停在动画结束位置
			animation.setDuration(150);
			mTabImg.startAnimation(animation);
		}
		
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}
	
	//选择按钮事件  下面主要处理系统菜单按钮事件，主要是退出的处理
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {  //获取 back键
    		//菜单操作
        	if(menu_display){         //如果 Menu已经打开 ，先关闭Menu
        		menuWindow.dismiss();
        		menu_display = false;
        		}
        	else {
        		Intent intent = new Intent();
            	intent.setClass(MainActivity.this,Exit.class);
            	startActivity(intent);
        	}
    	}
    	else if(keyCode == KeyEvent.KEYCODE_MENU){   //获取 Menu键			
			if(!menu_display){
				
				//获取LayoutInflater实例
				inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
				//这里的main布局是在inflate中加入的哦，以前都是直接this.setContentView()的吧？呵呵
				//该方法返回的是一个View的对象，是布局中的根
				layout = inflater.inflate(R.layout.main_menu, null);
				
				//下面我们要考虑了，我怎样将我的layout加入到PopupWindow中呢？？？很简单
				menuWindow = new PopupWindow(layout,LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT); //后两个参数是width和height
				//menuWindow.showAsDropDown(layout); //设置弹出效果
				//menuWindow.showAsDropDown(null, 0, layout.getHeight());
				menuWindow.showAtLocation(this.findViewById(R.id.mainactivity), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
				
				//如何获取我们main中的控件呢？也很简单
				mClose = (LinearLayout)layout.findViewById(R.id.menu_close);
				mCloseBtn = (LinearLayout)layout.findViewById(R.id.menu_close_btn);
				
				
				//下面对每一个Layout进行单击事件的注册吧。。。
				//比如单击某个MenuItem的时候，他的背景色改变
				//事先准备好一些背景图片或者颜色
				mCloseBtn.setOnClickListener (new View.OnClickListener() {					
					@Override
					public void onClick(View arg0) {						
						//Toast.makeText(Main.this, "退出", Toast.LENGTH_LONG).show();
						Intent intent = new Intent();
			        	intent.setClass(MainActivity.this,Exit.class);
			        	startActivity(intent);
			        	menuWindow.dismiss(); //响应点击事件之后关闭Menu
					}
				});				
				
				menu_display = true;				
			}else{
				//如果当前已经为显示状态，则隐藏起来
				menuWindow.dismiss();
				menu_display = false;
			}
			
			return false;
		}
    	
    	return false;
    }
	
	//设置标题栏右侧按钮的作用
	public void btnmainright(View v) {  
		Intent intent = new Intent (MainActivity.this,MainTopRightDialog.class);			
		startActivity(intent);	
     } 
	
	//接收解析到的数据
	public void startlisten(View v) {   
		

    } 
	
	//退出界面
	public void exit_settings(View v) {                           //退出  伪“对话框”，其实是一个activity
		Intent intent = new Intent (MainActivity.this,ExitFromSettings.class);			
		startActivity(intent);	
	 }
	
	//其他界面
	public void btn_shake(View v) {                                   
		
	}
}
    
    

