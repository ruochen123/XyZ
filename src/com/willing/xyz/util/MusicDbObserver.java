package com.willing.xyz.util;

import android.os.FileObserver;

public class MusicDbObserver extends FileObserver
{

	public MusicDbObserver(String path)
	{
		super(path, FileObserver.MODIFY);
	}

	@Override
	public void onEvent(int event, String path)
	{
		 
	}
}
