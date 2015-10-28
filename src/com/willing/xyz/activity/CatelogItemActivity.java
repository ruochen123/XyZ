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

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.BaseColumns;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.willing.xyz.R;
import com.willing.xyz.XyzApplication;
import com.willing.xyz.adapter.CatelogItemAdapter;
import com.willing.xyz.entity.Catelog;
import com.willing.xyz.entity.Music;
import com.willing.xyz.util.CatelogUtils;
import com.willing.xyz.util.MusicDatabaseHelper;

public class CatelogItemActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<ArrayList<Map<String, String>>>
{
	public static final String CATELOG_NAME = "catelogName";
 
	private static final int	LOADER_ID	= 4;
 
	private Button mPlayAll;
	private Button mSettle;
	private ListView mPlaylistItemListView;
	
	private PlayAllSongTask mPlayAllSongTask;
	
	private String mCatelogName;
	
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
		 
		
		if (mPlayAllSongTask != null)
		{
			mPlayAllSongTask.cancel(false);
		}
		
		super.onDestroy();
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
				
				if (app.getPlayService() != null)
				{
					app.getPlayService().addToPlayList(music, true);
					
					Intent intent = new Intent(CatelogItemActivity.this, PlayingActivity.class);
					startActivity(intent);
				}
			}
		});
		
		mPlayAll.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				mPlayAllSongTask = new PlayAllSongTask();
				mPlayAllSongTask.execute((SimpleAdapter)mPlaylistItemListView.getAdapter());
			}
		});
	}
	
	private void initView()
	{
		mPlaylistItemListView = (ListView) findViewById(R.id.lv_song);
		mPlayAll = (Button) findViewById(R.id.bt_play_allsong);
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
		CatelogItemAdapter adapter = new CatelogItemAdapter(CatelogItemActivity.this,
				result, R.layout.song_item, from, to, mCatelogName);
		
		if (mPlaylistItemListView != null)
		{
			mPlaylistItemListView.setAdapter(adapter);
		}
		
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
			
//			int deleteCount = 0;
			for (Iterator<Music> ite = musics.iterator(); ite.hasNext(); )
			{
				Music music = ite.next();
				HashMap<String, String> map = new HashMap<String, String>();
				
//				MusicDatabaseHelper helper = new MusicDatabaseHelper(getContext());
//				SQLiteDatabase db = helper.getWritableDatabase();
//				
//				Cursor cursor = db.query(MusicDatabaseHelper.TABLE_NAME,
//						new String[]{BaseColumns._ID}, MusicDatabaseHelper.PATH + " = ? ", 
//						new String[]{music.getPath()}, null, null, null);
//				
//				if (cursor == null || cursor.getCount() == 0)
//				{	
//					deleteCount++;
//					ite.remove();
//					continue;
//				}
				
				map.put(ALBUM, music.getAlbum()); 
				map.put(ARTIST, music.getArtist());
				map.put(PATH, music.getPath()); 
				map.put(TITLE, music.getTitle());
				map.put(DURATION, music.getDuration() + "");
				
				result.add(map);
			}
			
//			CatelogUtils.writeCatelogItem(getContext(), mCatelogName, musics);
//			
//			ArrayList<Catelog> catelogs = CatelogUtils.readCatelogs(getContext());
//		 
//			int index = catelogs.indexOf(new Catelog(mCatelogName, 0));
//			Catelog catelog = catelogs.get(index);
//			catelog.setCount(catelog.getCount() - deleteCount);
//			CatelogUtils.writeCatelogs(getContext(), catelogs);
			
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
