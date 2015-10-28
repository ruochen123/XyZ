package com.willing.xyz.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;

public class SingerItemAdapter extends AllSongAdapter 
{
	public SingerItemAdapter(Context context, int layout, Cursor c, String[] from,
			int[] to)
	{
		super(context, layout, c, from, to);
	}

	
}
