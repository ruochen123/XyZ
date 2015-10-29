package com.willing.xyz.fragment;

import static com.willing.xyz.util.MusicDatabaseHelper.ARTIST;
import static com.willing.xyz.util.MusicDatabaseHelper.TITLE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.willing.xyz.R;
import com.willing.xyz.activity.SingerItemActivity;
import com.willing.xyz.adapter.SingerAdapter;
import com.willing.xyz.entity.Singer;
import com.willing.xyz.util.MusicDatabaseHelper;




public class SingerFragment extends BaseFragment implements LoaderCallbacks<ArrayList<Singer>>
{
	private static final int	LOADER_ID	= 1;
	
	public static final String SINGER_CHANGED_FILE = "singer_changed.xyz";
	
	private ListView mSingerListView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_singer, container, false);
		
		initView(view);
		setupListener();
		
		getLoaderManager().initLoader(LOADER_ID, null, this);

		return view;
	}
 
	private void initView(View view)
	{
		mSingerListView = (ListView) view.findViewById(R.id.lv_singer);
	}
	
	private void setListViewAdapter(List<Map<String, String>> data)
	{
		String[] from = new String[]{
			"artist", 
			"count"
		};
		int[] to = new int[]{
			R.id.tv_singer_name,
			R.id.tv_singer_num
		};
		
		if (data == null)
		{
			data = new ArrayList<Map<String, String>>();
		}
		
		SimpleAdapter adapter = new SingerAdapter(getActivity(), data, R.layout.singer_item, from, to);
		
		mSingerListView.setAdapter(adapter);
	}

	private void setupListener()
	{
		setListViewAdapter(new ArrayList<Map<String, String>>());
		
		mSingerListView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				Intent intent = new Intent(SingerFragment.this.getActivity(), SingerItemActivity.class);
				
				TextView tmp = (TextView) view.findViewById(R.id.tv_singer_name);
				String name = tmp.getText().toString();
				intent.putExtra(SingerItemActivity.SINGER_NAME, name);
				
				startActivity(intent);
			}
		});
	}

//#start LoaderCallback
	@Override
	public Loader<ArrayList<Singer>> onCreateLoader(int arg0, Bundle arg1)
	{
		return new SingerLoader(getActivity());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onLoadFinished(Loader<ArrayList<Singer>> arg0,
			final ArrayList<Singer> singers)
	{
		
		new AsyncTask<ArrayList<Singer>, Void, ArrayList<Map<String, String>>>()
		{
			@Override
			protected ArrayList<Map<String, String>> doInBackground(ArrayList<Singer>... params)
			{
				ArrayList<Map<String, String>> data = new ArrayList<>();
				
				Singer singer = null;
				for (int i = 0; i < singers.size(); ++i)
				{
					Map<String, String> map = new HashMap<>();
					singer = singers.get(i);
					map.put(SingerAdapter.SINGER_NAME, singer.getName());
					map.put(SingerAdapter.SINGER_NUM, singer.getCount() + "สื");
					
					data.add(map);
				}
				return data;
			}
			
			@Override
			protected void onPostExecute(ArrayList<Map<String, String>> result)
			{
				setListViewAdapter(result);
			}
		}.execute(singers);
		
	}
	@Override
	public void onLoaderReset(Loader<ArrayList<Singer>> arg0)
	{
		mSingerListView.setAdapter(null);
	}
//#end	
	
	private static class SingerLoader extends AsyncTaskLoader<ArrayList<Singer>>
	{
		private ArrayList<Singer> mSingers;
		private Cursor mCursor;
		
		private SingerChangedObserver mObserver;

		public SingerLoader(Context context)
		{
			super(context);
 	
		}

		@Override
		public ArrayList<Singer> loadInBackground()
		{
			
			MusicDatabaseHelper helper = new MusicDatabaseHelper(
					getContext());

			SQLiteDatabase db = helper.getReadableDatabase();

			mCursor = db.query(MusicDatabaseHelper.TABLE_NAME, new String[]
			{ BaseColumns._ID, TITLE, ARTIST }, null, null, null, null,
					null);
			
		 
			mSingers = new ArrayList<>();
			Singer singer = null;
			
		 
			while (mCursor.moveToNext())
			{
				singer = new Singer();
				singer.setName(mCursor.getString(mCursor.getColumnIndex(MusicDatabaseHelper.ARTIST)));
				if (mSingers.contains(singer))
				{
					mSingers.get(mSingers.indexOf(singer)).inc();
				}
				else
				{
					singer.setCount(1);
					mSingers.add(singer);
				}
			}
			
			return mSingers;
		}

		@Override
		public void deliverResult(ArrayList<Singer> cur)
		{
			if (isReset())
			{
				mSingers = null;
				releaseResources();
				return;
			}
			
			mSingers = cur;
			
			if (isStarted())
			{
				super.deliverResult(cur);
			}

		}

		@Override
		protected void onStartLoading()
		{
			if (mSingers != null)
			{
				deliverResult(mSingers);
			}
			
			if (mObserver == null)
			{
				File file= getContext().getFileStreamPath(SINGER_CHANGED_FILE);
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
				mObserver = new SingerChangedObserver(file.getAbsolutePath());
				mObserver.startWatching();
			}
 
			if (takeContentChanged() || mSingers == null)
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
				mCursor.close();
				mCursor = null;
			}

		}

		@Override
		public void onCanceled(ArrayList<Singer> cur)
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
 
		private class SingerChangedObserver extends FileObserver
		{
			public SingerChangedObserver(String path)
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
