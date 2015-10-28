package com.willing.xyz.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.willing.xyz.XyzApplication;


public class BaseActivity extends ActionBarActivity
{
	public static XyzApplication app;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		app = XyzApplication.getInstance();
		
		app.addActivity(this);
	}
	
	@Override
	protected void onDestroy()
	{
		app.removeActivity(this);
		
		super.onDestroy();
	}
}
