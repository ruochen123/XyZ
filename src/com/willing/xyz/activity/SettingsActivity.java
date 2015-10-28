package com.willing.xyz.activity;

import android.content.Intent;
import android.os.Bundle;

import com.willing.xyz.R;
import com.willing.xyz.fragment.SettingsFragment;

public class SettingsActivity extends BaseActivity
{

	public static final String MIN_DURATION = "minDuration";
	
	public static final String IS_USE_NETWORK = "isUseNetwork";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(
				R.color.actionbar));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
		
 
	}
	
	@Override
	public Intent getParentActivityIntent()
	{
		return getIntent();
	}
}
