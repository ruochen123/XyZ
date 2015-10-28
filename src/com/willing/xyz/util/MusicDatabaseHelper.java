package com.willing.xyz.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class MusicDatabaseHelper extends SQLiteOpenHelper
{
	public static final String DBNAME = "music_db";
	public static final String TABLE_NAME = "music";
	public static final String TITLE = "title";
	public static final String ARTIST = "artist";
	public static final String ALBUM = "album";
	public static final String DURATION = "duration";
	public static final String PATH = "path";	
	 

	private static final String	SQL_CREATE_MUSIC_TABLE	= 
			"CREATE TABLE " + 
			TABLE_NAME + "( " + 
			BaseColumns._ID + " INTEGER PRIMARY KEY , " + 
			TITLE + " TEXT , " + 
			ARTIST + " TEXT , " +
			ALBUM + " TEXT , " + 
			DURATION + " INTEGER , " + 
			PATH + " TEXT " + ")"
			;

	public MusicDatabaseHelper(Context context)
	{
		super(context, DBNAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(SQL_CREATE_MUSIC_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		
	}
	
}