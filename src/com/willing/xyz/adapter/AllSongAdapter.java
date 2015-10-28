package com.willing.xyz.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.willing.xyz.R;
import com.willing.xyz.entity.Music;
import com.willing.xyz.util.MusicDatabaseHelper;
import com.willing.xyz.util.SongUtils;

/**
 * ”√”⁄AllSong∫ÕSinger Item
 * @author Willing
 *
 */
public class AllSongAdapter extends SimpleCursorAdapter 
{ 
	private Context mContext;
	private LayoutInflater mInflater;
	
	 

	public AllSongAdapter(Context context, int layout, Cursor c, String[] from,
			int[] to)
	{
		super(context, layout, c, from, to, 0);
		
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		final Cursor cursor = (Cursor) getItem(position);
		holder.title.setText(cursor.getString(cursor.getColumnIndex(MusicDatabaseHelper.TITLE)));
		holder.singer.setText(cursor.getString(cursor.getColumnIndex(MusicDatabaseHelper.ARTIST)));
		holder.album.setText(cursor.getString(cursor.getColumnIndex(MusicDatabaseHelper.ALBUM)));
		
		final View optionsPanel = convertView.findViewById(R.id.options_panel);
		optionsPanel.setVisibility(View.GONE);
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
						cursor.moveToPosition(position);
						Music music = SongUtils.cursorToMusic(cursor);
					 
						SongUtils.addToCatelogDialog(mContext, music);
					}
				});
			
				delete.setOnClickListener(new OnClickListener()
				{
					
					@Override
					public void onClick(View v)
					{
						cursor.moveToPosition(position);
						Music music = SongUtils.cursorToMusic(cursor);
						
						SongUtils.deleteSongDialog(mContext, music, false, null);

					}
				});
			
				info.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						cursor.moveToPosition(position);
						Music music = SongUtils.cursorToMusic(cursor);
						
						SongUtils.infoDialog(mContext, music);
					}
				});
			}
		});
		
		
		return convertView;
	}
	
	private static class ViewHolder
	{
		TextView title;
		TextView singer;
		TextView album;
		ImageButton options;
	}
 
}
