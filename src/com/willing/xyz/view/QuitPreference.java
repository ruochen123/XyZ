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
		builder.setPositiveButton("ȷ��", new OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{

				XyzApplication.getInstance().quitApp();
			}
		})
		.setNegativeButton("ȡ��", new OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				 
				
			}
		})
		.setTitle("ȷ���˳���")
		.setMessage("�˳��󲻻��ٺ�̨����");
		
		builder.create().show();
		
	}
}
