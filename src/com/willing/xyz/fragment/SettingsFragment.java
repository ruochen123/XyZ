package com.willing.xyz.fragment;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.willing.xyz.R;
import com.willing.xyz.XyzApplication;
import com.willing.xyz.activity.SettingsActivity;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener
{
	private SharedPreferences	mPreferences;
	private String	mMinDurationSummary;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
		
		mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
	 
		
		EditTextPreference minDuration = (EditTextPreference) findPreference(SettingsActivity.MIN_DURATION);
		String strMinDuration = mPreferences.getString(SettingsActivity.MIN_DURATION, "30");
		int intMinDuration = 30;
		try
		{
			intMinDuration = Integer.parseInt(strMinDuration);
		}
		catch (NumberFormatException ex)
		{
			
		}
		minDuration.setText(String.valueOf(intMinDuration));
		mMinDurationSummary = "过滤掉" + intMinDuration + "秒以下的歌曲";
		
//		CheckBoxPreference isUseNetwork = (CheckBoxPreference) findPreference(SettingsActivity.IS_USE_NETWORK);
//		isUseNetwork.setChecked(mPreferences.getBoolean(SettingsActivity.IS_USE_NETWORK, true));
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		EditTextPreference minDuration = (EditTextPreference) findPreference(SettingsActivity.MIN_DURATION);
		minDuration.setSummary(mMinDurationSummary);
		
		mPreferences.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		
		mPreferences.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key)
	{
		if (key.equals(SettingsActivity.MIN_DURATION))
		{
			EditTextPreference minDuration = (EditTextPreference) findPreference(SettingsActivity.MIN_DURATION);
			String strMinDuration = mPreferences.getString(SettingsActivity.MIN_DURATION, "30");
			int intMinDuration = 30;
			try
			{
				intMinDuration = Integer.parseInt(strMinDuration);
			}
			catch (NumberFormatException ex)
			{
				Toast.makeText(getActivity(), "请输入数字", Toast.LENGTH_SHORT).show();
			}
			mMinDurationSummary = "过滤掉" + intMinDuration + "秒以下的歌曲";
			minDuration.setSummary(mMinDurationSummary);
			
			XyzApplication.getInstance().setScanMinDuration(intMinDuration);
		}
	
	}
}
