package com.willing.xyz.util;

import java.util.ArrayList;

import android.database.Cursor;
import android.os.AsyncTask;

import com.willing.xyz.XyzApplication;
import com.willing.xyz.entity.Music;

public class PlayAllSongTask extends AsyncTask<Cursor, Void, ArrayList<Music>>
{
	@Override
	protected ArrayList<Music> doInBackground(Cursor... params)
	{
		ArrayList<Music> musics = new ArrayList<Music>();
		
		Cursor cursor = params[0];
		if (cursor == null || cursor.getCount() == 0)
		{
			return musics;
		}
		
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
		{
			if (isCancelled())
			{
				break;
			}
			
			Music music = new Music();
			music.setAlbum(cursor.getString(cursor.getColumnIndex(MusicDatabaseHelper.ALBUM)));
			music.setArtist(cursor.getString(cursor.getColumnIndex(MusicDatabaseHelper.ARTIST)));
			music.setPath(cursor.getString(cursor.getColumnIndex(MusicDatabaseHelper.PATH)));
			music.setTitle(cursor.getString(cursor.getColumnIndex(MusicDatabaseHelper.TITLE)));
			music.setDuration(cursor.getInt(cursor.getColumnIndex(MusicDatabaseHelper.DURATION)));
			
			musics.add(music);	
		}
		
		return musics;
	}
	
	@Override
	protected void onPostExecute(ArrayList<Music> result)
	{
		XyzApplication app = XyzApplication.getInstance();
		if (app == null)
		{
			return;
		}
		if (app.getPlayService() != null)
		{
			app.getPlayService().addToPlayList(result, true);
		}
	}
}