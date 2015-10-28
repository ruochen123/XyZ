package com.willing.xyz.view;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.willing.xyz.R;

public class AboutPreference extends DialogPreference
{

	public AboutPreference(Context context, AttributeSet set)
	{
		super(context, set);
		
        setPositiveButtonText(android.R.string.ok);
        setDialogTitle("����");
        setDialogMessage("�������ֲ�����\n" + "����: Willing Xyz\n" + "����: sxswilling@126.com");
        
        setDialogIcon(null);

	}
	
	

}
