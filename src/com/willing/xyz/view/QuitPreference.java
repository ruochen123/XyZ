package com.willing.xyz.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.willing.xyz.XyzApplication;

public class QuitPreference extends Preference
{

	public QuitPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	public View getView(View convertView, ViewGroup parent)
	{
		View view = super.getView(convertView, parent);
		
		view.setBackgroundColor(Color.RED);
		
		return view;
		
	}
	
	@Override
	protected void onClick()
	{
		super.onClick();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setPositiveButton("确定", new OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{

				XyzApplication.getInstance().quitApp();
			}
		})
		.setNegativeButton("取消", new OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				 
				
			}
		})
		.setTitle("确定退出？")
		.setMessage("退出后不会再后台播放");
		
		builder.create().show();
		
	}
}
