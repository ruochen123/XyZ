package com.willing.xyz;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.willing.xyz.activity.SettingsActivity;
import com.willing.xyz.service.MusicPlayService;
import com.willing.xyz.service.MusicPlayService.MusicPlayBinder;
import com.willing.xyz.service.ScanMusicService;

public class XyzApplication extends Application
{
	public static final String CATELOG_FILE_NAME = "catelogs.xyz";
	public static final String CATELOG_DIR = "catelogs";
	
	private static XyzApplication app;

	// 保存当前active的Activity
	private List<Activity> mActivityList;
	
	private MusicPlayService	mMusicPlayService;
	private ServiceConnection	mServiceConn;
	
	private boolean mDbChanged;
	
	
//#start 扫描歌曲的设置
	private int mScanMinDuration;    // 最小时长为多少
	private HashSet<File> mFilterPath;  // 不被扫描的路径
	
//#end
	
	public static XyzApplication getInstance()
	{
		return app;
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		app = this;
		
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
		mScanMinDuration = Integer.parseInt(preference.getString(SettingsActivity.MIN_DURATION, "30"));
		
		mActivityList = new LinkedList<Activity>();
		
		// test
		mFilterPath = new HashSet<File>();
		mFilterPath.add(new File("/sys"));
		mFilterPath.add(new File("/proc"));
		mScanMinDuration = 0;
		
		startMusicService();
	}

	private void startMusicService()
	{
		// 启动一个服务，用来扫描音乐
		Intent intent = new Intent(this, ScanMusicService.class);
		startService(intent);
		
		// 绑定播放服务，用来播放音乐
		Intent playIntent = new Intent(this, MusicPlayService.class);
		mServiceConn = new ServiceConnection()
		{
			
			@Override
			public void onServiceDisconnected(ComponentName name)
			{
				mMusicPlayService = null;
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service)
			{
				mMusicPlayService = ((MusicPlayBinder)service).getService();
			}
		};
		
		bindService(playIntent, mServiceConn, Context.BIND_AUTO_CREATE);
	}

	public MusicPlayService getPlayService()
	{
		return mMusicPlayService;
	}
	
	public void setDbChanged(boolean changed)
	{
		mDbChanged = changed;
	}
	public boolean isDbChanged()
	{
		return mDbChanged;
	}
	
//#start 添加移除当前active的Activity
	public void addActivity(Activity a)
	{
		mActivityList.add(a);
	}
	public void removeActivity(Activity a)
	{
		mActivityList.remove(a);
	}
//#end
 
//#start 扫描歌曲的设置

	public int getScanMinDuration()
	{
		return mScanMinDuration;
	}

	public void setScanMinDuration(int scanMinDuration)
	{
		mScanMinDuration = scanMinDuration;
	}

	public HashSet<File> getFilterPath()
	{
		return mFilterPath;
	}

	public void setFilterPath(HashSet<File> filterPath)
	{
		mFilterPath = filterPath;
	}
	
	public void addFilterPath(File path)
	{
		mFilterPath.add(path);
	}
	
	public boolean isContainInFilterPath(File path)
	{
		Iterator<File> ite = mFilterPath.iterator();
		File file = null;
		while (ite.hasNext())
		{
			file = ite.next();
			try
			{
				if (path.getCanonicalPath().startsWith(file.getCanonicalPath()))
				{
					return true;
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}
//#end

	public void quitApp()
	{
		for (Iterator<Activity> ite = mActivityList.iterator(); ite.hasNext(); )
		{
			Activity activity = ite.next();
			
			if (activity != null)
			{
				activity.finish();
			}
		}
		
		getPlayService().stopForeground(true);
		getPlayService().pause();
		unbindService(mServiceConn);
 
		
	}
	
	
}
