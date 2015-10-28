package com.willing.xyz.activity;

import java.lang.reflect.Field;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.ImageButton;
import android.widget.TextView;

import com.willing.xyz.R;
import com.willing.xyz.adapter.ViewPagerAdapter;
import com.willing.xyz.entity.Music;
import com.willing.xyz.fragment.AllSongFragment;
import com.willing.xyz.fragment.CatelogFragment;
import com.willing.xyz.fragment.SingerFragment;
import com.willing.xyz.service.MusicPlayService.MusicPlayerListener;
import com.willing.xyz.util.TimeUtils;

public class MainActivity extends BaseActivity implements MusicPlayerListener
{
	private ImageButton				mSmallPic;

	private ImageButton				mPause;

	private ImageButton				mNext;

	private TextView				mTitle;

	private TextView				mSinger;

	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initView();
		setupListener();
		initActionBar();
		setupTabAndViewPager();
	}



	@Override
	protected void onStart()
	{
		super.onStart();
		
		if (app.getPlayService() != null)
		{
			app.getPlayService().registerMusicPlayerListener(this);
		}
		
		updateAllUi();
	}
	
	@Override
	protected void onStop()
	{
		if (app.getPlayService() != null)
		{
			app.getPlayService().unregisterMusicPlayerListener(this);
		}
		
		super.onStop();
	}
	
	private void initView()
	{
		mSmallPic = (ImageButton) findViewById(R.id.ib_small_pic);
		mPause = (ImageButton) findViewById(R.id.ib_pause);
		mNext = (ImageButton) findViewById(R.id.ib_next);
		mTitle = (TextView) findViewById(R.id.tv_title);
		mSinger = (TextView) findViewById(R.id.tv_singer);
	}
	
	private void setupListener()
	{

		mSmallPic.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(MainActivity.this,
						PlayingActivity.class);

				startActivity(intent);
			}
		});

		mPause.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (app.getPlayService() == null)
				{
					return;
				}
				boolean isPlaying = app.getPlayService().isPlaying();
				isPlaying = !isPlaying;
				if (isPlaying)
				{
					mPause.setImageResource(R.drawable.pause);
					app.getPlayService().start();
				} else
				{
					mPause.setImageResource(R.drawable.start);
					app.getPlayService().pause();
				}
			}
		});

 

		mNext.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (app.getPlayService() != null)
				{
					app.getPlayService().next();
				}
			 
			}
		});
	}

	
	private void initActionBar()
	{
		forceShowActionBarOverflowMenu();
		
		ActionBar actionBar = getSupportActionBar();

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		actionBar.setBackgroundDrawable(getResources().getDrawable(
				R.color.actionbar));
		actionBar.setStackedBackgroundDrawable(getResources().getDrawable(
				R.color.actionbar_tab));
	}
	
	/**
	 * 安装Tab和ViewPager，并设置监听器以关联
	 */
	@SuppressWarnings("deprecation")
	private void setupTabAndViewPager()
	{
		ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
		ActionBar actionBar = getSupportActionBar();

		ViewPagerAdapter adapter = new ViewPagerAdapter(
				getSupportFragmentManager(), viewPager, actionBar);

		adapter.addFragment(new CatelogFragment());
		adapter.addFragment(new SingerFragment());
		adapter.addFragment(new AllSongFragment());

		viewPager.setAdapter(adapter);
		viewPager.setOnPageChangeListener(adapter);

		actionBar.addTab(
				actionBar.newTab().setText("播放列表").setTabListener(adapter), 0,
				true);
		actionBar.addTab(
				actionBar.newTab().setText("歌手").setTabListener(adapter), 1);
		actionBar.addTab(
				actionBar.newTab().setText("歌曲").setTabListener(adapter), 2);

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
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
//		SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//		SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
//		
//		searchView.setSearchableInfo(manager.getSearchableInfo(new ComponentName(this, SearchActivity.class)));
//		searchView.setSubmitButtonEnabled(true);
//		searchView.setQueryRefinementEnabled(true);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		int id = item.getItemId();
		switch (id)
		{
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			
			startActivity(intent);
			
			return true;
//			
//		case R.id.action_search:
//			onSearchRequested();
//			
//			return true;
			
		default:
				break;
		}
	 
		return super.onOptionsItemSelected(item);
	}

	public void updateAllUi()
	{
		if (app.getPlayService() == null)
		{
			return;
		}

		// 更新 Start/Pause按钮
		boolean isPlaying = app.getPlayService().isPlaying();
		if (isPlaying)
		{
			mPause.setImageResource(R.drawable.pause);
		} else
		{
			mPause.setImageResource(R.drawable.start);
		}

		// 更新歌手、专辑、时间
		Music info = app.getPlayService().getPlayingMusic();
		if (info == null)
		{
			return;
		}
		mTitle.setText(info.getTitle());
		mSinger.setText(info.getArtist());

		// 专辑封面
	}

 
//#start implements MusicPlayerListener
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
		Music info = app.getPlayService().getPlayingMusic();
		if (info != null)
		{
			mTitle.setText(info.getTitle());
			mSinger.setText(info.getArtist());
		}
	}
//#end
	
}
