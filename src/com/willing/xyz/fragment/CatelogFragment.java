package com.willing.xyz.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileObserver;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.willing.xyz.R;
import com.willing.xyz.XyzApplication;
import com.willing.xyz.activity.CatelogItemActivity;
import com.willing.xyz.adapter.CatelogAdapter;
import com.willing.xyz.entity.Catelog;
import com.willing.xyz.util.CatelogUtils;


public class CatelogFragment extends BaseFragment implements LoaderCallbacks<ArrayList<Catelog>>
{
	private static final int	LOADER_ID	= 3;
	private ListView 		mPlaylistListView;
	private Button 			mNewPlaylist;
	private Button			mSettle; 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View view =  inflater.inflate(R.layout.fragment_catelog, container, false);
		
		initView(view);
		setupListener();
		
		getLoaderManager().initLoader(LOADER_ID, null, this);
		
		return view;
	}

	
	private void initView(View view)
	{
		mPlaylistListView = (ListView)view.findViewById(R.id.lv_playlist);
		mNewPlaylist = (Button) view.findViewById(R.id.bt_new_playlist);
		mSettle = (Button) view.findViewById(R.id.bt_settle);
	 
		setListViewAdapter(null);
	}
	
	private void setListViewAdapter(List<Map<String, String>> data)
	{
		String[] from = new String[]{
			CatelogAdapter.CATELOG_NAME, 
			CatelogAdapter.CATELOG_COUNT
		};
		int[] to = new int[]{
			R.id.tv_playlist_name,
			R.id.tv_playlist_num
		};
		
		if (data == null)
		{
			data = new ArrayList<Map<String, String>>();
		}
		
		SimpleAdapter adapter = new CatelogAdapter(getActivity(), data, R.layout.catelog_item, from, to);
		
		mPlaylistListView.setAdapter(adapter);
	}



	private void setupListener()
	{
		mPlaylistListView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				TextView tvPlaylistName = (TextView) view.findViewById(R.id.tv_playlist_name);
				String name = tvPlaylistName.getText().toString();
				
				Intent intent = new Intent(CatelogFragment.this.getActivity(), CatelogItemActivity.class);
				intent.putExtra(CatelogItemActivity.CATELOG_NAME, name);
				startActivity(intent);
			}
		});
		
		mNewPlaylist.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				AlertDialog.Builder build = new AlertDialog.Builder(CatelogFragment.this.getActivity());
				build.setTitle(R.string.new_playlist_dialog);
				final EditText edit = new EditText(CatelogFragment.this.getActivity());
				build.setView(edit);
				build.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						String name = edit.getText().toString().trim();
						if ("".equals(name))
						{
							Toast.makeText(CatelogFragment.this.getActivity(), R.string.playlist_name_is_empty, Toast.LENGTH_LONG).show();
							return;
						}
						
						if (!CatelogUtils.getCatelogsName(getActivity()).contains(name))
						{
							CatelogUtils.createCatelog(getActivity(), name);
						}
						else
						{
							Toast.makeText(CatelogFragment.this.getActivity(), R.string.new_playlist_same, Toast.LENGTH_SHORT).show();
						}
					}
				});
				build.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
					 
				});
				build.setCancelable(true);
				
				build.create().show();
				
			}
		});
		
	}

	
//#start LoaderCallback
	@Override
	public Loader<ArrayList<Catelog>> onCreateLoader(int arg0, Bundle arg1)
	{
		return new CatelogLoader(getActivity());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onLoadFinished(Loader<ArrayList<Catelog>> arg0,
			final ArrayList<Catelog> catelogs)
	{
		new AsyncTask<ArrayList<Catelog>, Void, ArrayList<Map<String, String>>>()
		{
			@Override
			protected ArrayList<Map<String, String>> doInBackground(ArrayList<Catelog>... params)
			{
				ArrayList<Map<String, String>> data = new ArrayList<>();
				
				Catelog catelog = null;
				for (int i = 0; i < catelogs.size(); ++i)
				{
					Map<String, String> map = new HashMap<>();
					catelog = catelogs.get(i);
					map.put(CatelogAdapter.CATELOG_NAME, catelog.getName());
					map.put(CatelogAdapter.CATELOG_COUNT, catelog.getCount() + "สื");
					
					data.add(map);
				}
				return data;
			}
			
			@Override
			protected void onPostExecute(ArrayList<Map<String, String>> result)
			{
				setListViewAdapter(result);
			}
		}.execute(catelogs);
				
	}

	@Override
	public void onLoaderReset(Loader<ArrayList<Catelog>> arg0)
	{
		
	}
//#end
	
	private static class CatelogLoader extends AsyncTaskLoader<ArrayList<Catelog>>
	{
		private ArrayList<Catelog> mCatelogs;
		private FileObserver	mObserver;

		public CatelogLoader(Context context)
		{
			super(context);
		}

		@Override
		public ArrayList<Catelog> loadInBackground()
		{
			mCatelogs = CatelogUtils.readCatelogs(getContext());
 
			return mCatelogs;
		}

		@Override
		public void deliverResult(ArrayList<Catelog> cur)
		{
			if (isReset())
			{
				mCatelogs = null;
				return;
			}
			
			mCatelogs = cur;
			
			if (isStarted())
			{
				super.deliverResult(cur);
			}

		}

		@Override
		protected void onStartLoading()
		{
			if (mCatelogs != null)
			{
				deliverResult(mCatelogs);
			}

			if (mObserver == null)
			{
				mObserver = new CatelogFileObserver(getContext().getFileStreamPath(XyzApplication.CATELOG_FILE_NAME).getAbsolutePath());
				mObserver.startWatching();
			}
			
			if (takeContentChanged() || mCatelogs == null)
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
			mCatelogs = null;
		}


		private class CatelogFileObserver extends FileObserver
		{

			public CatelogFileObserver(String path)
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
