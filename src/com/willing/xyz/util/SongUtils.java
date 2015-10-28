package com.willing.xyz.util;

import static com.willing.xyz.util.MusicDatabaseHelper.ALBUM;
import static com.willing.xyz.util.MusicDatabaseHelper.ARTIST;
import static com.willing.xyz.util.MusicDatabaseHelper.DURATION;
import static com.willing.xyz.util.MusicDatabaseHelper.PATH;
import static com.willing.xyz.util.MusicDatabaseHelper.TITLE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.willing.xyz.R;
import com.willing.xyz.XyzApplication;
import com.willing.xyz.activity.SingerItemActivity;
import com.willing.xyz.entity.Catelog;
import com.willing.xyz.entity.Music;
import com.willing.xyz.fragment.SingerFragment;
import com.willing.xyz.service.MusicPlayService;

public class SongUtils
{
	public static void addToCatelogDialog(final Context context, final ArrayList<Music> musics)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.add_to_catelog);
		
		String[] items = readCatelogs(context);
 
		if (items == null || items.length == 0)
		{
			items = new String[1];
			items[0] = context.getResources().getString(R.string.new_playlist);
			
			builder.setItems(items, new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					CatelogUtils.newCatelogDialog(context);
				}
			});
			items = readCatelogs(context);
		}
		final String[] strs = items;
		builder.setItems(items, new OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				CatelogUtils.addToCatelog(context, strs[which], musics);
			}
		});
		
		builder.setCancelable(true);
		builder.create().show();
	}
 
	
	private static String[] readCatelogs(Context context)
	{
		ArrayList<Catelog> catelogs;
		String[] items = null;
	 
		catelogs = CatelogUtils.readCatelogs(context);
		items = new String[catelogs.size()];
		for (int i = 0; i < catelogs.size(); ++i)
		{
			items[i] = catelogs.get(i).getName();
		}
 
		return items;
	}

	public static Music cursorToMusic(Cursor cursor)
	{
		Music music = new Music();
		
		music.setAlbum(cursor.getString(cursor.getColumnIndex(ALBUM)));
		music.setArtist(cursor.getString(cursor.getColumnIndex(ARTIST)));
		music.setTitle(cursor.getString(cursor.getColumnIndex(TITLE)));
		music.setPath(cursor.getString(cursor.getColumnIndex(PATH)));
		music.setDuration(cursor.getInt(cursor.getColumnIndex(DURATION)));
		
		return music;
	}

	public static Music mapToMusic(Map<String, String> map)
	{
		Music music = new Music();
		music.setAlbum(map.get(ALBUM));
		music.setArtist(map.get(ARTIST));
		music.setPath(map.get(PATH));
		music.setTitle(map.get(TITLE));
		music.setDuration(Integer.parseInt(map.get(DURATION)));
		
		return music;
	}
	
	public static void deleteSongDialog(final Context context, final Music music, final boolean fromCatelog, final String catelogName)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("确定删除 " + music.getTitle() + " 吗?");
		
		if (fromCatelog)
		{
			builder.setNeutralButton(R.string.delete_from_catelog, new OnClickListener()
			{
				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					CatelogUtils.deleteFromCatelog(context, catelogName, music);
				}
			});
		}
 
		builder.setPositiveButton(R.string.delete_song, new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{		
				if (deleteSong(context, music))
				{
					if (fromCatelog)
					{
						CatelogUtils.deleteFromCatelog(context, catelogName, music);
					}
					Toast.makeText(context, R.string.delete_successed, Toast.LENGTH_SHORT).show();
				}
				else
				{
					Toast.makeText(context, R.string.delete_failed, Toast.LENGTH_SHORT).show();
				}
			}
		});
	 

	
		builder.setNegativeButton(R.string.cancel_dialog, new OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		builder.setCancelable(true);
		builder.create().show();
	}

	public static void infoDialog(Context context, Music music)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("详细信息");
		
		builder.setMessage("标题: \t" + music.getTitle() 
				+ "\n歌手: \t" + music.getArtist()
				+ "\n专辑: \t" + music.getAlbum()
				+ "\n时长: \t" + TimeUtils.parseDuration(music.getDuration())
				+ "\n路径: \t" + music.getPath()
				);
		
		builder.setCancelable(true);
		builder.create().show();
		
	}

	public static void showPlayList(Activity activity, View viewPlayList, int y)
	{		
		final MusicPlayService service = XyzApplication.getInstance().getPlayService();
		
		if (service == null)
		{
			return;
		}
		
		View view = activity.getLayoutInflater().inflate(R.layout.popupwindow_play_list, null);
		
		ListView listView = (ListView) view.findViewById(R.id.play_list);
		
		// 设置播放列表
		ArrayList<Music> infoList = service.getPlayList();
		List<String> data = new LinkedList<String>();
		

		int count = infoList.size();
		for (int i = 0; i < count; ++i)
		{
			Music info = infoList.get(i);
			String songSinger = info.getTitle() + " - " + info.getArtist();
			
			data.add(songSinger);
		}
	
		ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, R.layout.play_list_item, R.id.text, data);
		listView.setAdapter(adapter);
		
		// 显示播放列表
		WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);  
		int height = windowManager.getDefaultDisplay().getHeight();
		final PopupWindow popupWindow = new PopupWindow(view, LayoutParams.MATCH_PARENT, height / 2, true);
		
		popupWindow.setOutsideTouchable(true);
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
		popupWindow.showAtLocation(viewPlayList, Gravity.NO_GRAVITY, 0, height / 2 - y);
		
		listView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				popupWindow.dismiss();
				
				service.setIndex(position);
				service.playNewMusic();
			}
		});
	}

	public static boolean deleteSong(Context context, Music music)
	{
		// 删除文件
		File file = new File(music.getPath());
		boolean successed = file.delete();
		// 更新数据库
		
		if (successed)
		{
			MusicDatabaseHelper helper = new MusicDatabaseHelper(context);
			SQLiteDatabase db = helper.getWritableDatabase();
			
			db.delete(MusicDatabaseHelper.TABLE_NAME, 
					MusicDatabaseHelper.PATH + " = ? ", new String[]{music.getPath()});
			
			db.releaseReference();
			
			// 改变Singer的文件，以通知Singer
			try
			{
				FileOutputStream out = context.openFileOutput(SingerFragment.SINGER_CHANGED_FILE, Context.MODE_PRIVATE);
				out.write((int) System.currentTimeMillis());
				out.close();
				
				out = context.openFileOutput(SingerItemActivity.SINGER_ITEM_CHANGED_FILE, Context.MODE_PRIVATE);
				out.write((int)System.currentTimeMillis());
				out.close();
				
			} 
			catch (IOException e)
			{ 
				e.printStackTrace();
			}
			
			
		}
		return successed;
	}
	
	// 批量删除歌曲
	public static void deleteSongs(Context context,
			ArrayList<String> musics)
	{
		File file = null;
		boolean successed = false;
		MusicDatabaseHelper helper = new MusicDatabaseHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();
		for (int i = 0; i < musics.size(); ++i)
		{
			file = new File(musics.get(i));
			successed = file.delete();
			
			// 更新数据库
			if (successed)
			{
				db.delete(MusicDatabaseHelper.TABLE_NAME, 
						MusicDatabaseHelper.PATH + " = ? ", new String[]{musics.get(i)});
				
				// 改变Singer的文件，以通知Singer
				try
				{
					FileOutputStream out = context.openFileOutput(SingerFragment.SINGER_CHANGED_FILE, Context.MODE_PRIVATE);
					out.write((int) System.currentTimeMillis());
					out.close();
					
					out = context.openFileOutput(SingerItemActivity.SINGER_ITEM_CHANGED_FILE, Context.MODE_PRIVATE);
					out.write((int)System.currentTimeMillis());
					out.close();
					
				} 
				catch (IOException e)
				{
					e.printStackTrace();
				}
			
			}
		}

		db.releaseReference();
	}


}
