package com.willing.xyz.activity;

import static com.willing.xyz.util.MusicDatabaseHelper.ALBUM;
import static com.willing.xyz.util.MusicDatabaseHelper.ARTIST;
import static com.willing.xyz.util.MusicDatabaseHelper.DURATION;
import static com.willing.xyz.util.MusicDatabaseHelper.PATH;
import static com.willing.xyz.util.MusicDatabaseHelper.TITLE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.ActionBarActivity;
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
import android.widget.ListView;

import com.willing.xyz.R;
import com.willing.xyz.adapter.SingerItemAdapter;
import com.willing.xyz.entity.Music;
import com.willing.xyz.util.MusicDatabaseHelper;
import com.willing.xyz.util.PlayAllSongTask;
import com.willing.xyz.util.SongUtils;

public class SingerItemActivity extends BaseActivity implements LoaderCallbacks<Cursor>
{
	// 当歌曲被删除时，修改该文件，以通知该Activity重新加载数据（用Loader）。
	// 因为该Activity每次onCreate时会重新加载数据，所以修改该文件的情况应该是在该Activity onResume情况下。
	public static final String	SINGER_ITEM_CHANGED_FILE	= "singer_item_changed";
	
	public static final String SINGER_NAME = "singerName";
	
	private static final int	LOAD_ID	= 5;
	
	private ListView	mSingerItemListView;
	private SingerItemAdapter mListAdapter;
	
	private PlayAllSongTask mPlayAllSongTask;
	private Button mPlayAll;
	
	private String mSingerName;
	
	// ListView前面的View，用来在ActionMode启动时，隐藏。
	private View	mHeader;	

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
		
		Bundle args = new Bundle();
		args.putString(SINGER_NAME, mSingerName);
		getLoaderManager().initLoader(LOAD_ID, args, this);
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
		mSingerItemListView = (ListView) findViewById(R.id.lv_song);
		mPlayAll = (Button) findViewById(R.id.bt_play_allsong);
		mHeader = findViewById(R.id.ll_header);
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
				cancelPlayAllSongTask();
				
				mPlayAllSongTask = new PlayAllSongTask();
				mPlayAllSongTask.execute(mListAdapter.getCursor());
			}
		});
	
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
		mSingerItemListView.setMultiChoiceModeListener(new MultiChoiceModeListener()
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
				mListAdapter.setActionModeStarted(false);
				 
				mListAdapter.notifyDataSetChanged();
			
				mHeader.setVisibility(View.VISIBLE);
			}
			
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu)
			{
				mode.getMenuInflater().inflate(R.menu.song, menu);
				
				mHeader.setVisibility(View.GONE);
				
				mListAdapter.setActionModeStarted(true);
				 
				mListAdapter.notifyDataSetChanged();
				
				return true;
			}
			
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item)
			{
				switch (item.getItemId())
				{
				case R.id.delete:
					
					AlertDialog.Builder builder = new AlertDialog.Builder(SingerItemActivity.this);
					builder.setTitle("确定删除所选歌曲？");
					builder.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							Cursor cursor = null;
							ArrayList<String> musics = new ArrayList<String>();
							SparseBooleanArray checked =  mSingerItemListView.getCheckedItemPositions();
							for (int i = 0; i < checked.size(); ++i)
							{
								cursor = (Cursor) mListAdapter.getItem(checked.keyAt(i));
								musics.add(cursor.getString(cursor.getColumnIndex(MusicDatabaseHelper.PATH)));
							}
							
							SongUtils.deleteSongs(SingerItemActivity.this, musics, true, false, null);
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
					
					Music music = null;
					Cursor cursor = null;
					ArrayList<Music> musics = new ArrayList<Music>();
					SparseBooleanArray checked =  mSingerItemListView.getCheckedItemPositions();
					for (int i = 0; i < checked.size(); ++i)
					{
						cursor = (Cursor) mListAdapter.getItem(checked.keyAt(i));
						music = new Music();
						music.setAlbum(cursor.getString(cursor.getColumnIndex(ALBUM)));
						music.setTitle(cursor.getString(cursor.getColumnIndex(TITLE)));
						music.setPath(cursor.getString(cursor.getColumnIndex(PATH)));
						music.setArtist(cursor.getString(cursor.getColumnIndex(ARTIST)));
						music.setDuration(cursor.getInt(cursor.getColumnIndex(DURATION)));
						
						musics.add(music);
					}
					
					
					SongUtils.addToCatelogDialog(SingerItemActivity.this, musics);
					
					return true;
				}
				return false;
			}
			
			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position,
					long id, boolean checked)
			{
				View view = mSingerItemListView.getChildAt(position - mSingerItemListView.getFirstVisiblePosition());
				CheckBox checkbox = (CheckBox) view.findViewById(R.id.cb_checked);
				checkbox.setChecked(mSingerItemListView.isItemChecked(position));
			}
		});
		mSingerItemListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
	}
	
	@Override
	public Intent getParentActivityIntent()
	{
		return getIntent();
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
