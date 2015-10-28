package com.willing.xyz.service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.tag.TagException;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.willing.xyz.XyzApplication;
import com.willing.xyz.entity.Music;
import com.willing.xyz.util.MusicDatabaseHelper;
import com.willing.xyz.util.ParseMusicFile;

public class ScanMusicService extends IntentService 
{
	
	// 通过扫描的后缀名
	private static String[] exts = {"mp3", "aac"};
	
	private SQLiteDatabase mDb;
	private MusicDatabaseHelper mDbHelper; 
	
	private HashSet<Music> mMusics;
	
	public ScanMusicService(String name)
	{
		super(name);
	}
	
	public ScanMusicService()
	{
		super("ScanMusicService-Thread");
	}

	private static XyzApplication app;
	


	@Override
	public void onCreate()
	{
		super.onCreate();
		
		app = XyzApplication.getInstance();
		
		mDbHelper = new MusicDatabaseHelper(this);
		
		mMusics = new HashSet<Music>();
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		mMusics.clear();
		
		
		scanFile(File.listRoots()[0]);
		
		
		mDb = mDbHelper.getWritableDatabase();
		
		mDb.beginTransaction();
		
		// 清除之前的数据
		mDb.delete(MusicDatabaseHelper.TABLE_NAME, null, null);
		// 插入最新查找到的数据
		Music music = null;
		
		Iterator<Music> ite = mMusics.iterator();
		while (ite.hasNext())
		{
			music = ite.next();
			
			ContentValues values = new ContentValues(5);
			values.put(MusicDatabaseHelper.TITLE, music.getTitle());
			values.put(MusicDatabaseHelper.ARTIST, music.getArtist());
			values.put(MusicDatabaseHelper.ALBUM, music.getAlbum());
			values.put(MusicDatabaseHelper.DURATION, music.getDuration());
			values.put(MusicDatabaseHelper.PATH, music.getPath());
			
			
			mDb.insert(MusicDatabaseHelper.TABLE_NAME, null, values);			
		}
		mDb.setTransactionSuccessful();
		mDb.endTransaction();
	}
	
	// 扫描文件,并添加到数据库中
	private void scanFile(File file)
	{
		if (file.isDirectory())
		{
			File[] files = file.listFiles(new FileFilter()
			{
				
				@Override
				public boolean accept(File pathname)
				{
					if (app != null && app.isContainInFilterPath(pathname))
					{
						return false;
					}
					if (pathname.isDirectory())
					{
						return true;
					}
					else
					{
						for (int i = 0; i < exts.length; ++i)
						{
							if (pathname.getName().endsWith(exts[i]))
							{
								return true;
							}
						}
					}
					return false;
				}
			});
			if (files == null)
			{
				return;
			}
			for (int i = 0; i < files.length; ++i)
			{
				scanFile(files[i]);
			}
		}
		else
		{
			handleFile(file);
		}
	}
	
	private void handleFile(File file)
	{
		Music music = null;
		try
		{
			music = ParseMusicFile.parse(file);
		} 
		catch (CannotReadException | IOException | TagException
				| InvalidAudioFrameException e)
		{
			return;
		}

		
		if (music.getDuration() < app.getScanMinDuration())
		{
			return;
		}
		
		mMusics.add(music);
		
	}
	
}
