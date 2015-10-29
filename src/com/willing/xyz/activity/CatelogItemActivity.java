package com.willing.xyz.activity;

import static com.willing.xyz.util.MusicDatabaseHelper.ALBUM;
import static com.willing.xyz.util.MusicDatabaseHelper.ARTIST;
import static com.willing.xyz.util.MusicDatabaseHelper.DURATION;
import static com.willing.xyz.util.MusicDatabaseHelper.PATH;
import static com.willing.xyz.util.MusicDatabaseHelper.TITLE;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileObserver;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.willing.xyz.R;
import com.willing.xyz.XyzApplication;
import com.willing.xyz.adapter.CatelogAdapter;
import com.willing.xyz.adapter.CatelogItemAdapter;
import com.willing.xyz.entity.Catelog;
import com.willing.xyz.entity.Music;
import com.willing.xyz.util.CatelogUtils;
import com.willing.xyz.util.SongUtils;

public class CatelogItemActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<ArrayList<Map<String, String>>>
{
	public static final String CATELOG_NAME = "catelogName";
 
	private static final int	LOADER_ID	= 4;
 
	private Button mPlayAll;
	private ListView mPlaylistItemListView;
	private CatelogItemAdapter mAdapter;
	
	private PlayAllSongTask mPlayAllSongTask;
	
	private String mCatelogName;
	
	private boolean 	mIsActionModeStarted;

	private View	mHeader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_song);

		initView();
		setupListener();
		
		Intent intent = getIntent();
		mCatelogName = (String) intent.getExtras().get(CatelogItemActivity.CATELOG_NAME);
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.RED));
		actionBar.setTitle(mCatelogName);
		
		Bundle args = new Bundle();
		args.putString(CATELOG_NAME, mCatelogName);
		getLoaderManager().initLoader(LOADER_ID, args, CatelogItemActivity.this);
	}

	@Override
	protected void onDestroy()
	{
		cancelPlayAllSongTask();
		
		super.onDestroy();
	}
	
	private void cancelPlayAllSongTask()
	{
		if (mPlayAllSongTask != null && mPlayAllSongTask.getStatus() != AsyncTask.Status.FINISHED)
		{
			mPlayAllSongTask.cancel(false);
		}
	}

	private void initView()
	{
		mPlaylistItemListView = (ListView) findViewById(R.id.lv_song);
		mPlayAll = (Button) findViewById(R.id.bt_play_allsong);
		mHeader = findViewById(R.id.ll_header);
	}
	
	private void setupListener()
	{
		mPlaylistItemListView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				ListAdapter adapter = mPlaylistItemListView.getAdapter();
				@SuppressWarnings("unchecked")
				Map<String, String> map = (Map<String, String>) adapter.getItem(position);
				
				Music music = new Music();
				music.setAlbum(map.get(ALBUM)); 
				music.setArtist(map.get(ARTIST));
				music.setPath(map.get(PATH));
				music.setTitle(map.get(TITLE));
				music.setDuration(Integer.parseInt(map.get(DURATION)));
				
				File file = new File(music.getPath());
				if (!file.exists())
				{
					// 如果该文件不存在，则显示对话框，用来询问用户是否删除不存在歌曲
					AlertDialog.Builder builder = new Builder(CatelogItemActivity.this);
					builder.setMessage(R.string.is_delete_invalid_song);
					builder.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							CatelogUtils.deleteInvalidFromCatelog(CatelogItemActivity.this, mCatelogName);
						}
						 
					});
					builder.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
					builder.setCancelable(true);
					builder.create().show();
				}
				else
				{
					if (app.getPlayService() != null)
					{
						app.getPlayService().addToPlayList(music, true);
						
						Intent intent = new Intent(CatelogItemActivity.this, PlayingActivity.class);
						startActivity(intent);
					}
				}
			}
		});
		
		mPlayAll.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				cancelPlayAllSongTask();
				mPlayAllSongTask = new PlayAllSongTask();
				mPlayAllSongTask.execute((SimpleAdapter)mPlaylistItemListView.getAdapter());
			}
		});
	
		setListViewAdapter(new ArrayList<Map<String, String>>());
		mPlaylistItemListView.setMultiChoiceModeListener(new MultiChoiceModeListener()
		{
			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu)
			{
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void onDestroyActionMode(ActionMode mode)
			{
				mAdapter.setActionModeStarted(false);
				mIsActionModeStarted = false;
				mAdapter.notifyDataSetChanged();
 
				mHeader.setVisibility(View.VISIBLE);
			}
			
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu)
			{
				mode.getMenuInflater().inflate(R.menu.catelog, menu);
				
				mHeader.setVisibility(View.GONE);
 			
				mAdapter.setActionModeStarted(true);
				mIsActionModeStarted = true;
				mAdapter.notifyDataSetChanged();
				
				return true;
			}
			
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item)
			{
				switch (item.getItemId())
				{
				case R.id.delete:
					
					AlertDialog.Builder builder = new AlertDialog.Builder(CatelogItemActivity.this);
					builder.setTitle("确定删除所选歌曲？");
					builder.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							Map<String, String> map = null;
							ArrayList<String> musics = new ArrayList<String>();
							SparseBooleanArray checked =  mPlaylistItemListView.getCheckedItemPositions();
							for (int i = 0; i < checked.size(); ++i)
							{
								map = (Map<String, String>) mAdapter.getItem(checked.keyAt(i));
								musics.add(map.get(PATH));
							}
							
							SongUtils.deleteSongs(CatelogItemActivity.this, musics, false, true, mCatelogName);
						}
					});
					builder.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
					builder.setCancelable(true);
					builder.create().show();
					
					return true;
				case R.id.add_to_catelog:
					Map<String, String> map = null;
					Music music = null;
					ArrayList<Music> musics = new ArrayList<Music>();
					SparseBooleanArray checked =  mPlaylistItemListView.getCheckedItemPositions();
					for (int i = 0; i < checked.size(); ++i)
					{
						map = (Map<String, String>) mAdapter.getItem(checked.keyAt(i));
						music = new Music();
						music.setAlbum(map.get(ALBUM));
						music.setArtist(map.get(ARTIST));
						music.setPath(map.get(PATH));
						music.setTitle(map.get(TITLE));
						music.setDuration(Integer.parseInt(map.get(DURATION)));
						
						musics.add(music);
					}
					SongUtils.addToCatelogDialog(CatelogItemActivity.this, musics);
					
					return true;
				case R.id.remove_from_catelog:
					
					AlertDialog.Builder builder2 = new AlertDialog.Builder(CatelogItemActivity.this);
					builder2.setTitle("确定从列表中移除所选歌曲？");
					builder2.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							Map<String, String> map = null;
							Music music = null;
							ArrayList<Music> musics = new ArrayList<Music>();
							SparseBooleanArray checked =  mPlaylistItemListView.getCheckedItemPositions();
							for (int i = 0; i < checked.size(); ++i)
							{
								map = (Map<String, String>) mAdapter.getItem(checked.keyAt(i));
								music = new Music();
								music.setPath(map.get(PATH));
								
								musics.add(music);
							}
							CatelogUtils.deleteFromCatelog(CatelogItemActivity.this, mCatelogName, musics);
						}
					});
					builder2.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
					builder2.setCancelable(true);
					builder2.create().show();

					return true;
				}
				return false;
			}
			
			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position,
					long id, boolean checked)
			{
				View view = mPlaylistItemListView.getChildAt(position - mPlaylistItemListView.getFirstVisiblePosition());
				CheckBox checkbox = (CheckBox) view.findViewById(R.id.cb_checked);
				checkbox.setChecked(mPlaylistItemListView.isItemChecked(position));
			}
		});
		mPlaylistItemListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
	}

	private void setListViewAdapter(ArrayList<Map<String, String>> data)
	{
		String[] from = new String[]{
				TITLE,
				ARTIST,
				ALBUM
		};
		int[] to = new int[]{
			R.id.tv_title,
			R.id.tv_singer,
			R.id.tv_album
		};
		mAdapter = new CatelogItemAdapter(CatelogItemActivity.this,
				data, R.layout.song_item, from, to, mCatelogName, mIsActionModeStarted);
		
		if (mPlaylistItemListView != null)
		{
			mPlaylistItemListView.setAdapter(mAdapter);
		}
	}
	
	@Override
	public Intent getParentActivityIntent()
	{
		return getIntent();
	}

//#start LoaderCallbacks
	@Override
	public Loader<ArrayList<Map<String, String>>> onCreateLoader(int arg0,
			Bundle arg1)
	{
		String name = arg1.getString(CATELOG_NAME);
		// 不能返回非静态内部类
		return new CatelogItemLoader(this, name); 
	}

	@Override
	public void onLoadFinished(Loader<ArrayList<Map<String, String>>> arg0,
			ArrayList<Map<String, String>> result)
	{
		setListViewAdapter(result);
	}

	@Override
	public void onLoaderReset(Loader<ArrayList<Map<String, String>>> arg0)
	{
	}
//#end
 
	private static class CatelogItemLoader extends AsyncTaskLoader<ArrayList<Map<String, String>>>
	{
		private ArrayList<Map<String, String>> mData;
		private FileObserver	mObserver;
		private String mCatelogName;

		public CatelogItemLoader(Context context, String name)
		{
			super(context);
			
			mCatelogName = name;
		}

		@Override
		public ArrayList<Map<String, String>> loadInBackground()
		{
			ArrayList<Music> musics = CatelogUtils.readCatelogItem(getContext(), mCatelogName);
			ArrayList<Map<String, String>> result = new ArrayList<>();
			
 
			for (Iterator<Music> ite = musics.iterator(); ite.hasNext(); )
			{
				Music music = ite.next();
				HashMap<String, String> map = new HashMap<String, String>();
				
				map.put(ALBUM, music.getAlbum()); 
				map.put(ARTIST, music.getArtist());
				map.put(PATH, music.getPath()); 
				map.put(TITLE, music.getTitle());
				map.put(DURATION, music.getDuration() + "");
				
				result.add(map);
			}
			
 
			
			return result;
		}

		@Override
		public void deliverResult(ArrayList<Map<String, String>> cur)
		{
			if (isReset())
			{
				mData = null;
				return;
			}
			
			mData = cur;
			
			if (isStarted())
			{
				super.deliverResult(cur);
			}

		}

		@Override
		protected void onStartLoading()
		{
			if (mData != null)
			{
				deliverResult(mData);
			}

			if (mObserver == null)
			{
				String path = getContext().getDir(XyzApplication.CATELOG_DIR, Context.MODE_PRIVATE).getAbsolutePath() 
						+ File.separator + mCatelogName;
				mObserver = new CatelogItemFileObserver(path);
				mObserver.startWatching();
				
				Log.i("test", "path : " + path);
			}
			
			if (takeContentChanged() || mData == null)
			{
				forceLoad();
			}
		}

		@Override
		protected void onStopLoading()
		{
			cancelLoad();
		}

		@Override
		protected void onReset()
		{
			onStopLoading();

			if (mObserver != null)
			{
				mObserver.stopWatching();
				mObserver = null;
			}
			mData = null;
		}


		private class CatelogItemFileObserver extends FileObserver
		{

			public CatelogItemFileObserver(String path)
			{
				super(path, FileObserver.MODIFY);
			}

			@Override
			public void onEvent(int event, String path)
			{
				onContentChanged();
			}
		}
	}

	
	private class PlayAllSongTask extends AsyncTask<SimpleAdapter, Void, ArrayList<Music>>
	{

		@Override
		protected ArrayList<Music> doInBackground(SimpleAdapter... params)
		{
			ArrayList<Music> musics = new ArrayList<>();
			
			for (int i = 0; i < params[0].getCount(); ++i)
			{
				if (isCancelled())
				{
					break;
				}
				
				Music music = new Music();
				@SuppressWarnings("unchecked")
				Map<String, String> map = (Map<String, String>) params[0].getItem(i);
				
				music.setAlbum(map.get("album"));
				music.setArtist(map.get("artist"));
				music.setPath(map.get("path"));
				music.setTitle(map.get("map"));
				music.setDuration(Integer.parseInt(map.get("duration")));
				
				musics.add(music);
			}
			return musics;
		}
		
		@Override
		protected void onPostExecute(ArrayList<Music> result)
		{
			if (app.getPlayService() != null)
			{
				app.getPlayService().addToPlayList(result, true);
			}
		}
		
	}
	
}
