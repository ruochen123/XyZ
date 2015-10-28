package com.willing.xyz.activity;

import java.lang.reflect.Field;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.willing.xyz.R;
import com.willing.xyz.entity.Music;
import com.willing.xyz.service.MusicPlayService;
import com.willing.xyz.service.MusicPlayService.MusicPlayerListener;
import com.willing.xyz.util.SongUtils;
import com.willing.xyz.util.TimeUtils;

/**
 * 播放界面
 * @author Willing
 *
 */
public class PlayingActivity extends BaseActivity implements MusicPlayerListener
{  
	private ImageButton	mPlayMode;
	private ImageButton	mPause;
	private ImageButton	mPlayList;
	private View	mControlPanel;
	private ImageButton	mNext;
	private ImageButton	mPre;
 
	private MusicPlayService mService;

	private TextView	mArtist;

	private TextView	mTitle;
	
	private Thread updateThread;

	private SeekBar	mSeekBar;

	private TextView	mCurTime;

	private TextView	mTotalTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_playing);
		
		mService = app.getPlayService();
		
		forceShowActionBarOverflowMenu();
		
		initView();
		setupListener();
 

//		// 从外部应用启动
//		Uri uri = intent.getData();
//	
//		if (uri != null)
//		{
//			String strUri = uri.toString();
//			
//			int index = 0;
//			// content未实现
//			if (strUri.contains("content:"))
//			{
//				index = 9;
//			}
//			else if (strUri.contains("file:"))
//			{
//				index = 7;
//			}
//			String path = strUri.substring(index);
//			if (MediaUtils.isExsitInMediaStore(path))
//			{
//				MusicInfo info = MediaUtils.getMusicFromMediaStore(path);
//				
//				XyzApplication.app.setWhichList(WhichList.OTHER);
//				XyzApplication.app.setWhichPos(0);
//				ArrayList<MusicInfo> list = XyzApplication.app.getPlayList();
//				list.clear();
//				list.add(info);
//				
//				mMusicPlayService.playNewMusic();
//			}
//		}
//		


	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		
		if (mService != null)
		{
			mService.registerMusicPlayerListener(this);
		}
	
		// 更新UI线程
		updateThread = new UpdateThread();
		updateThread.start();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		updateAllUi();
		
		
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		
		if (mService != null)
		{
			mService.unregisterMusicPlayerListener(this);
		}
		
		updateThread.interrupt();
	}
	
 
	
	// 需要每秒更新一次的UI
	private void updateUi()
	{	
		if (mService == null)
		{
			return;
		}
		
		mCurTime.setText(TimeUtils.parseDuration(mService.getCurPos() / 1000));
		mSeekBar.setProgress((int) mService.getCurPos() / 1000);
		
		// 更新 Start/Pause按钮
		boolean isPlaying = mService.isPlaying();
		if (isPlaying)
		{
			mPause.setImageResource(R.drawable.pause);
		} else
		{
			mPause.setImageResource(R.drawable.start);
		}
	 
	}
	
	// 更新所有可能改变的UI
	private void updateAllUi()
	{
		if (mService == null)
		{
			return;
		}
		
		Music info = mService.getPlayingMusic();
		if (info != null)
		{
			mTitle.setText(info.getTitle());
			mArtist.setText(info.getArtist());
			
			mCurTime.setText(TimeUtils.parseDuration(mService.getCurPos()));
			mTotalTime.setText(TimeUtils.parseDuration(info.getDuration()));
			
			mSeekBar.setMax(info.getDuration());
		}	
		
		if (mService.isPlaying())
		{
			mPause.setImageResource(R.drawable.pause);
		}
		else
		{
			mPause.setImageResource(R.drawable.start);
		}
	}
//
//	@Override
//	public Intent getParentActivityIntent()
//	{
//		return getIntent();
//	}

	private View initActionBar()
	{
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.actionbar));
	
		View actionBarView = getLayoutInflater().inflate(R.layout.actionbar_play, null);
		ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(Gravity.CENTER);
		actionBar.setCustomView(actionBarView, layoutParams);
		return actionBarView;
	}

	private void setupListener()
	{
		
		mPlayMode.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				int mode = mService.nextPlayMode();
				mPlayMode.setImageResource(MusicPlayService.PLAY_MODE[mode]);
			}
		});
		
		mPause.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				boolean isPlaying = mService.isPlaying();
				isPlaying = !isPlaying;
				if (isPlaying)
				{
					mPause.setImageResource(R.drawable.pause);
					mService.start();
				}
				else
				{
					mPause.setImageResource(R.drawable.start);
					mService.pause();
				}
			 
			}
		});
		
		mPlayList.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				SongUtils.showPlayList(PlayingActivity.this, mPlayList, mControlPanel.getHeight());
			}
		});
		
		mPre.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				mService.pre();
			}
		});
		
		mNext.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				mService.next();
			}
		});
		
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser)
			{
				if (!fromUser)
				{
					return;
				}
				mService.seekTo(progress * 1000);
			}
		});
	
	
		

	}

	private void initView()
	{
		mPlayMode = (ImageButton) findViewById(R.id.ib_play_mode);
		mPause = (ImageButton)findViewById(R.id.ib_pause);
		mPlayList = (ImageButton)findViewById(R.id.ib_play_list);
		mControlPanel = findViewById(R.id.rl_control_panel);
		mPre = (ImageButton)findViewById(R.id.ib_pre);
		mNext = (ImageButton)findViewById(R.id.ib_next);
		mSeekBar = (SeekBar)findViewById(R.id.sb_play_progress);
		mCurTime = (TextView)findViewById(R.id.tv_cur_time);
		mTotalTime = (TextView)findViewById(R.id.tv_total_time);
 
		
		boolean isPlaying = false;
		if (mService != null)
		{
			isPlaying = mService.isPlaying();
		}
		
		if (isPlaying)
		{
			mPause.setImageResource(R.drawable.pause);
		}
		else
		{
			mPause.setImageResource(R.drawable.start);
		}
		
		int mode = 0;
		if (mService != null)
		{
			mode = mService.getPlayMode();
		}
		mPlayMode.setImageResource(MusicPlayService.PLAY_MODE[mode]);
 
		View actionBarView = initActionBar();
		mTitle = (TextView) actionBarView.findViewById(R.id.play_title);
		mArtist = (TextView) actionBarView.findViewById(R.id.play_artist);
		// 设置SeekBar
		Music info = null;
		if (mService != null)
		{
			info = mService.getPlayingMusic();
		}
		if (info != null)
		{
			mSeekBar.setMax(info.getDuration());
			
			mTotalTime.setText(TimeUtils.parseDuration(info.getDuration()));
			
			mTitle.setText(info.getTitle());
			mArtist.setText(info.getArtist()); 
		}

	}
	
	/**
	 * 强制显示overflowmenu
	 */
	private void forceShowActionBarOverflowMenu()
	{
		try{
			ViewConfiguration config=ViewConfiguration.get(this);
			Field menuKeyField=ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if(menuKeyField!=null){
			menuKeyField.setAccessible(true);
			menuKeyField.setBoolean(config,false);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public Intent getParentActivityIntent()
	{
		return getIntent();
	}
	
//#start MusicPlayerListener
	@Override
	public void pause()
	{
		mPause.setImageResource(R.drawable.start);
	}

	@Override
	public void start()
	{
		mPause.setImageResource(R.drawable.pause);
	}
	
	@Override
	public void changeMusic()
	{
		Music info = mService.getPlayingMusic();
		if (info != null)
		{
			mTitle.setText(info.getTitle());
			mArtist.setText(info.getArtist());
			mTotalTime.setText(TimeUtils.parseDuration(info.getDuration()));
			mSeekBar.setMax(info.getDuration());
		}
	}
//#end
	
	private class UpdateThread extends Thread
	{
		public void run()
		{
			while (true)
			{
				try
				{
					Thread.sleep(1000);
				} catch (InterruptedException e)
				{
					return;
				}
				if (interrupted())
				{
					return;
				}
				
				if (mService == null)
				{
					break;
				}
				if (mService.isPlaying())
				{
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							updateUi();
						}
					});
					
					
				}
			
			}
		}
	}

}