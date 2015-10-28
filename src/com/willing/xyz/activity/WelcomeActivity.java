package com.willing.xyz.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import com.willing.xyz.R;

public class WelcomeActivity extends BaseActivity
{

	private static final int	DELAY_MILLS	= 2000;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	 
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		

		
		// 在指定的延迟后，进入MainActivity
		new Handler().postDelayed(new Runnable()
		{
			
			@Override
			public void run()
			{
				Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
				
				startActivity(intent);
				
				WelcomeActivity.this.finish();
				// 指定Activity切换时的动画
				WelcomeActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
			}
		}, DELAY_MILLS);
	}
 
 
}
