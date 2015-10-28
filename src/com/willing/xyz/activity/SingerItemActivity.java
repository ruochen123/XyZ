package com.willing.xyz.activity;

import static com.willing.xyz.util.MusicDatabaseHelper.ALBUM;
import static com.willing.xyz.util.MusicDatabaseHelper.ARTIST;
import static com.willing.xyz.util.MusicDatabaseHelper.DURATION;
import static com.willing.xyz.util.MusicDatabaseHelper.PATH;
import static com.willing.xyz.util.MusicDatabaseHelper.TITLE;

import java.io.File;
import java.io.IOException;

import android.app.LoaderManager.LoaderCallbacks;
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
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.willing.xyz.R;
import com.willing.xyz.adapter.SingerItemAdapter;
import com.willing.xyz.entity.Music;
import com.willing.xyz.util.MusicDatabaseHelper;
import com.willing.xyz.util.PlayAllSongTask;

public class SingerItemActivity extends BaseActivity implements LoaderCallbacks<Cursor>
{
	public static final String	SINGER_ITEM_CHANGED_FILE	= "singer_item_changed";
	
	private ListView	mSingerItemListView;
	private SimpleCursorAdapter mListAdapter;
	
	private LoadSingerItemTask loadSingerItemTask;
	private PlayAllSongTask playAllsongTask;
	
	private Button mPlayAll;
	
	private String mSingerName;
	
	public static final String SINGER_NAME = "singerName";

	private static final int	LOAD_ID	= 5;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_song);
		
		initView();
		setupListener();
		
		Intent intent = getIntent();
		mSingerName = (String) intent.getExtras().getString(SINGER_NAME);
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.RED));
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setTitle(mSingerName);
 
		loadSingerItemTask = new LoadSingerItemTask();
		loadSingerItemTask.execute(mSingerName);
		
		Bundle args = new Bundle();
		args.putString(SINGER_NAME, mSingerName);
		getLoaderManager().initLoader(LOAD_ID, args, this);
	}
	
	
	
	@Override
	protected void onDestroy()
	{
		loadSingerItemTask.cancel(false);
		if (playAllsongTask != null)
		{
			playAllsongTask.cancel(false);
		}
		
		super.onDestroy();
	}
	
	private void initView()
	{
		mSingerItemListView = (ListView) findViewById(R.id.lv_song);
		mPlayAll = (Button) findViewById(R.id.bt_play_allsong);
		
		String[] from = 
			{
				MusicDatabaseHelper.TITLE,
				MusicDatabaseHelper.ARTIST,
				MusicDatabaseHelper.ALBUM
			};
		int[] to = 
			{
				R.id.tv_title,
				R.id.tv_singer,
				R.id.tv_album
			};
		mListAdapter = new SingerItemAdapter(this, R.layout.song_item, null, from, to);
		mSingerItemListView.setAdapter(mListAdapter);
	}
	private void setupListener()
	{
		mSingerItemListView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				Music music = new Music();
				Cursor cursor = (Cursor)mListAdapter.getItem(position);
				music.setAlbum(cursor.getString(cursor.getColumnIndex(MusicDatabaseHelper.ALBUM)));
				music.setArtist(cursor.getString(cursor.getColumnIndex(MusicDatabaseHelper.ARTIST)));
				music.setPath(cursor.getString(cursor.getColumnIndex(MusicDatabaseHelper.PATH)));
				music.setTitle(cursor.getString(cursor.getColumnIndex(MusicDatabaseHelper.TITLE)));
				music.setDuration(cursor.getInt(cursor.getColumnIndex(MusicDatabaseHelper.DURATION)));
				
				
				if (app.getPlayService() != null)
				{
					app.getPlayService().addToPlayList(music, true);

					Intent intent = new Intent(SingerItemActivity.this, PlayingActivity.class);
					startActivity(intent);
				}
			}
			
		});
		
		
		mPlayAll.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				playAllsongTask = new PlayAllSongTask();
				playAllsongTask.execute(mListAdapter.getCursor());
			}
		});
	}
	
	@Override
	public Intent getParentActivityIntent()
	{
		return getIntent();
	}
	
	private class LoadSingerItemTask extends AsyncTask<String, Void, Cursor>
	{

		@Override
		protected Cursor doInBackground(String... params)
		{
			Cursor cursor = null;
			
			MusicDatabaseHelper helper = new MusicDatabaseHelper(SingerItemActivity.this);
			SQLiteDatabase db = helper.getReadableDatabase();
			
			cursor = db.query(MusicDatabaseHelper.TABLE_NAME, new String[]
			{ BaseColumns._ID, TITLE, ARTIST, ALBUM, DURATION, PATH }, ARTIST + " = ?", params, null, null,
					null);
			
			
			return cursor;
		}
		
		@Override
		protected void onPostExecute(Cursor result)
		{
			if (mListAdapter != null)
			{
				mListAdapter.changeCursor(result);
			}
		}
	}


	//#start LoaderCallback
		@Override
		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1)
		{
			return new SingerItemLoader(this, arg1.getString(SINGER_NAME));
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void onLoadFinished(Loader<Cursor> arg0,
				final Cursor cursor)
		{
			mListAdapter.changeCursor(cursor);
		}
		@Override
		public void onLoaderReset(Loader<Cursor> arg0)
		{
			mListAdapter.changeCursor(null);
		}
	//#end	
		
	private static class SingerItemLoader extends AsyncTaskLoader<Cursor>
	{
			private String mSingerName;

			private Cursor mCursor;
		 
			private FileObserver mObserver;

			public SingerItemLoader(Context context, String name)
			{
				super(context);
				mSingerName = name;
			}

			@Override
			public Cursor loadInBackground()
			{
				MusicDatabaseHelper helper = new MusicDatabaseHelper(getContext());
				SQLiteDatabase db = helper.getReadableDatabase();
				
				mCursor = db.query(MusicDatabaseHelper.TABLE_NAME, new String[]
				{ BaseColumns._ID, TITLE, ARTIST, ALBUM, DURATION, PATH }, ARTIST + " = ?", new String[]{mSingerName}, null, null,
						null);
				
				
				return mCursor;
			}

			@Override
			public void deliverResult(Cursor cur)
			{
				if (isReset())
				{
					releaseResources();
					return;
				}
				
				mCursor = cur;
				
				if (isStarted())
				{
					super.deliverResult(cur);
				}

			}

			@Override
			protected void onStartLoading()
			{
				if (mCursor != null)
				{
					deliverResult(mCursor);
				}
				
				if (mObserver == null)
				{
					File file= getContext().getFileStreamPath(SINGER_ITEM_CHANGED_FILE);
					if (!file.exists())
					{
						try
						{
							file.createNewFile();
						} catch (IOException e)
						{
							 
							e.printStackTrace();
						}
					}
					mObserver = new SingerItemChangedObserver(file.getAbsolutePath());
					mObserver.startWatching();
				}
	 
				if (takeContentChanged() || mCursor == null)
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

				releaseResources();

			}

			@Override
			public void onCanceled(Cursor cur)
			{
				super.onCanceled(cur);

				// The load has been canceled, so we should release the resources
				// associated with 'data'.
				releaseResources();
			}

			private void releaseResources()
			{
				if (mCursor != null)
				{
					mCursor.close();
					mCursor = null;
				}
			}
	 
			private class SingerItemChangedObserver extends FileObserver
			{
				public SingerItemChangedObserver(String path)
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


}
