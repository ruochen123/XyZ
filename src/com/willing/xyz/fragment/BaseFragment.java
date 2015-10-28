package com.willing.xyz.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.willing.xyz.XyzApplication;

public class BaseFragment extends Fragment
{
	public static XyzApplication app;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		app = XyzApplication.getInstance();
	}
}
