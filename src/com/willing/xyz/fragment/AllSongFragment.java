package com.willing.xyz.fragment;

import static com.willing.xyz.util.MusicDatabaseHelper.ALBUM;
import static com.willing.xyz.util.MusicDatabaseHelper.ARTIST;
import static com.willing.xyz.util.MusicDatabaseHelper.DURATION;
import static com.willing.xyz.util.MusicDatabaseHelper.PATH;
import static com.willing.xyz.util.MusicDatabaseHelper.TITLE;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import com.willing.xyz.R;
import com.willing.xyz.adapter.AllSongAdapter;
import com.willing.xyz.entity.Music;
import com.willing.xyz.util.MusicDatabaseHelper;
import com.willing.xyz.util.PlayAllSongTask;
import com.willing.xyz.util.SongUtils;

public class AllSongFragment extends BaseFragment implements
		LoaderCallbacks<Cursor>
{
	
	private ListView			mAllSongListView;
	private AllSongAdapter 		mListAdapter;
	private Button				mPlayAllSong;

	private PlayAllSongTask mPlayAllSongTask;
	private View	mHeader;
	
	private static final int	LOADER_ID	= 1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_allsong, container,
				false);

		initView(view);
		setupListener();

		getLoaderManager().initLoader(LOADER_ID, null, this);

		return view;
	}
 
	@Override
	public void onDestroyView()
	{
		cancelPlayAllSongTask();
		
		super.onDestroyView();
	}
	
	private void initView(View view)
	{
		mAllSongListView = (ListView) view.findViewById(R.id.lv_allsong);
		mPlayAllSong = (Button) view.findViewById(R.id.bt_play_allsong);
		mHeader = view.findViewById(R.id.ll_header);

	}

	private void setupListener()
	{

		mAllSongListView.setOnItemClickListener(new OnItemClickListener()
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
				}
			}
		});

		mPlayAllSong.setOnClickListener(new OnClickListener()
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
		
		mListAdapter = new AllSongAdapter(getActivity(), R.layout.song_item, null, from, to);
		
		mAllSongListView.setAdapter(mListAdapter);
		mAllSongListView.setMultiChoiceModeListener(new MultiChoiceModeListener()
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
				
				// 显示ActionBar的Tab
				ActionBarActivity activity = (ActionBarActivity)getActivity();
				ActionBar actionbar = activity.getSupportActionBar();
				actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
				mHeader.setVisibility(View.VISIBLE);
			}
			
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu)
			{
				mode.getMenuInflater().inflate(R.menu.song, menu);
				
				mHeader.setVisibility(View.GONE);
				
				// 隐藏ActionBar的Tab
				ActionBarActivity activity = (ActionBarActivity)getActivity();
				ActionBar actionbar = activity.getSupportActionBar();
				actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
				
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
					
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle("确定删除所选歌曲？");
					builder.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							Cursor cursor = null;
							ArrayList<String> musics = new ArrayList<String>();
							SparseBooleanArray checked =  mAllSongListView.getCheckedItemPositions();
							for (int i = 0; i < checked.size(); ++i)
							{
								cursor = (Cursor) mListAdapter.getItem(checked.keyAt(i));
								musics.add(cursor.getString(cursor.getColumnIndex(MusicDatabaseHelper.PATH)));
							}
							
							SongUtils.deleteSongs(getActivity(), musics, false, false, null);
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
					SparseBooleanArray checked =  mAllSongListView.getCheckedItemPositions();
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
					
					
					SongUtils.addToCatelogDialog(getActivity(), musics);
					
					return true;
				}
				return false;
			}
			
			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position,
					long id, boolean checked)
			{
				View view = mAllSongListView.getChildAt(position - mAllSongListView.getFirstVisiblePosition());
				CheckBox checkbox = (CheckBox) view.findViewById(R.id.cb_checked);
				checkbox.setChecked(mAllSongListView.isItemChecked(position));
			}
		});
		mAllSongListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
	}
	
	private void cancelPlayAllSongTask()
	{
		if (mPlayAllSongTask != null && mPlayAllSongTask.getStatus() != AsyncTask.Status.FINISHED)
		{
			mPlayAllSongTask.cancel(false);
		}
	}

//#start LoaderCallback方法
	@Override
	public android.support.v4.content.Loader<Cursor> onCreateLoader(int arg0,
			Bundle arg1)
	{
		return new AllSongLoader(AllSongFragment.this.getActivity());
	}

	@Override
	public void onLoadFinished(android.support.v4.content.Loader<Cursor> arg0,
			Cursor arg1)
	{
	 
		mListAdapter.changeCursor(arg1);
	}

	@Override
	public void onLoaderReset(android.support.v4.content.Loader<Cursor> arg0)
	{
		mListAdapter.swapCursor(null);
	}
//#end 
	
	private static class AllSongLoader extends AsyncTaskLoader<Cursor>
	{
		private Cursor	mCursor;
		private MusicDbObserver	mObserver;

		public AllSongLoader(Context context)
		{
			super(context);
 
		}

		@Override
		public Cursor loadInBackground()
		{
			MusicDatabaseHelper helper = new MusicDatabaseHelper(
					getContext());

			SQLiteDatabase db = helper.getReadableDatabase();

			mCursor = db.query(MusicDatabaseHelper.TABLE_NAME, new String[]
			{ BaseColumns._ID, TITLE, ARTIST, ALBUM, DURATION, PATH }, null, null, null, null,
					null);
			
			return mCursor;
		}

		@Override
		public void deliverResult(Cursor cur)
		{
			if (isReset())
			{
				releaseResources(cur);
				return;
			}

			Cursor oldCur = mCursor;
			mCursor = cur;
			
			if (isStarted())
			{
				super.deliverResult(cur);
			}

			if (oldCur != null && oldCur != cur)
			{
				releaseResources(oldCur);
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
				mObserver = new MusicDbObserver(getContext().getDatabasePath(MusicDatabaseHelper.DBNAME).getAbsolutePath());
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
			if (mCursor != null)
			{
				releaseResources(mCursor);
				mCursor = null;
			}

		}

		@Override
		public void onCanceled(Cursor cur)
		{
			super.onCanceled(cur);

			// The load has been canceled, so we should release the resources
			// associated with 'data'.
			releaseResources(cur);
		}

		private void releaseResources(Cursor cur)
		{
			if (cur != null)
			{
				cur.close();
				cur = null;
			}
			
		}

		
		private class MusicDbObserver extends FileObserver
		{

			public MusicDbObserver(String path)
			{
				super(path, FileObserver.MODIFY);
				
			}

			@Override
			public void onEvent(int event, String path)
			{
				onContentChanged();
				app.setDbChanged(true);
			}
		}
	}


}
