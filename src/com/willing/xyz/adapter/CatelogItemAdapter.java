package com.willing.xyz.adapter;

import static com.willing.xyz.util.MusicDatabaseHelper.ALBUM;
import static com.willing.xyz.util.MusicDatabaseHelper.ARTIST;
import static com.willing.xyz.util.MusicDatabaseHelper.TITLE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.willing.xyz.R;
import com.willing.xyz.entity.Music;
import com.willing.xyz.util.SongUtils;

public class CatelogItemAdapter extends SimpleAdapter
{	
	private Context mContext;
	private LayoutInflater mInflater;
	
	private String mCatelog;
	
	private boolean mActionModeStarted;
	
	public CatelogItemAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to, String catelog, boolean isStarted)
	{
		super(context, data, resource, from, to);
		
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mCatelog = catelog;
		mActionModeStarted = isStarted;
	}

	public void setActionModeStarted(boolean started)
	{
		mActionModeStarted = started;
	}
	public boolean isActionModeStarted()
	{
		return mActionModeStarted;
	}
	
	
	@Override
	public View getView(final int position, View convertView, ViewGroup viewGroup)
	{
		ViewHolder holder = null;
		if (convertView == null)
		{
			convertView = mInflater.inflate(R.layout.song_item, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.tv_title);
			holder.singer = (TextView) convertView.findViewById(R.id.tv_singer);
			holder.album = (TextView) convertView.findViewById(R.id.tv_album);
			holder.options = (ImageButton) convertView.findViewById(R.id.ib_options);
			holder.checkbox = (CheckBox) convertView.findViewById(R.id.cb_checked);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		final Map<String, String> map = (Map<String, String>) getItem(position);
		holder.title.setText(map.get(TITLE));
		holder.singer.setText(map.get(ARTIST));
		holder.album.setText(map.get(ALBUM));
		
		final View optionsPanel = convertView.findViewById(R.id.options_panel);
		optionsPanel.setVisibility(View.GONE);
	
		if (isActionModeStarted())
		{
			if (holder.checkbox.getVisibility() == View.GONE)
			{
				holder.checkbox.setVisibility(View.VISIBLE);
			}
			ListView listView = (ListView) viewGroup;
			holder.checkbox.setChecked(listView.isItemChecked(position));
		}
		else
		{
			if (holder.checkbox.getVisibility() == View.VISIBLE)
			{
				holder.checkbox.setVisibility(View.GONE);
			}
			holder.options.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (optionsPanel.getVisibility() == View.GONE)
					{
						optionsPanel.setVisibility(View.VISIBLE);
						 
					}
					else
					{
						optionsPanel.setVisibility(View.GONE);
					}
					View addToCatelog = optionsPanel.findViewById(R.id.add_to_catelog);
					View delete = optionsPanel.findViewById(R.id.delete_song);
					View info = optionsPanel.findViewById(R.id.song_info);
					
					addToCatelog.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							ArrayList<Music> musics = new ArrayList<Music>();
							musics.add(SongUtils.mapToMusic(map));
							SongUtils.addToCatelogDialog(mContext, musics);
						}
					});
				
					delete.setOnClickListener(new OnClickListener()
					{
						
						@Override
						public void onClick(View v)
						{
							SongUtils.deleteSongDialog(mContext, SongUtils.mapToMusic(map), true, mCatelog);

						}
					});
				
					info.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							SongUtils.infoDialog(mContext, SongUtils.mapToMusic(map));
						}
					});
				}
			});
		}
		
		
		return convertView;
	}
	
	private static class ViewHolder
	{
		TextView title;
		TextView singer;
		TextView album;
		ImageButton options;
		CheckBox checkbox;
	}
}
