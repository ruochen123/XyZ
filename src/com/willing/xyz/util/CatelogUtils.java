package com.willing.xyz.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.util.SparseBooleanArray;
import android.widget.EditText;
import android.widget.Toast;

import com.willing.xyz.R;
import com.willing.xyz.XyzApplication;
import com.willing.xyz.activity.CatelogItemActivity;
import com.willing.xyz.entity.Catelog;
import com.willing.xyz.entity.Music;

public class CatelogUtils
{
	// 读取存储列表信息的汇总文件
	public static ArrayList<Catelog> readCatelogs(Context context) 
	{
		ArrayList<Catelog> list = new ArrayList<Catelog>();
		DataInputStream inData = null;
		try
		{
			File file = context.getFileStreamPath(XyzApplication.CATELOG_FILE_NAME);
			if (!file.exists())
			{
				file.createNewFile();
			}
			InputStream in = context.openFileInput(XyzApplication.CATELOG_FILE_NAME);
			inData = new DataInputStream(in);
	
			int count = inData.readInt();
			for (int i = 0; i < count; ++i)
			{
				String name = inData.readUTF();
				int num = inData.readInt();
				
				Catelog catelog = new Catelog(name, num);
				
				list.add(catelog);
			}
		}  
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			if (inData != null)
			{
				try
				{
					inData.close();
					
				} catch (IOException e)
				{
					 
					e.printStackTrace();
				}
			}
		}
		return list;
	}

	// 写列表到总文件
	public static void writeCatelogs(Context context, ArrayList<Catelog> catelogs)
	{
		DataOutputStream outData = null;
		try
		{
			OutputStream out = context.openFileOutput(XyzApplication.CATELOG_FILE_NAME, Context.MODE_PRIVATE);
			outData = new DataOutputStream(out);
			
			Catelog cate = null;
			outData.writeInt(catelogs.size());
			for (Iterator<Catelog> ite = catelogs.iterator(); ite.hasNext();)
			{
				 cate = ite.next();
				 
				 outData.writeUTF(cate.getName());
				 outData.writeInt(cate.getCount());
			}
			
		} catch (FileNotFoundException e)
		{
			
			e.printStackTrace();
		} catch (IOException e)
		{
			
			e.printStackTrace();
		}
		finally
		{
			if (outData != null)
			{
				try
				{
					outData.close();
					
				} catch (IOException e)
				{
					 
					e.printStackTrace();
				}
			}
		}
	}
	
	// 得到所有列表的名字
	public static ArrayList<String> getCatelogsName(Context context)
	{
		ArrayList<Catelog> list = readCatelogs(context);
			
		if (list == null)
		{
			return new ArrayList<String>();
		}
		ArrayList<String> catelogs = new ArrayList<String>(list.size());
		
		for (int i = 0; i < list.size(); ++i)
		{
			catelogs.add(list.get(i).getName());
		}
		
		return catelogs;
	}
	
	// 创建新列表
	public static boolean createCatelog(Context context, String name)
	{
 
		File dir = context.getDir(XyzApplication.CATELOG_DIR, Context.MODE_PRIVATE);
		
		String path = dir.getAbsolutePath() + File.separator + name;
		File file = new File(path);
		
		boolean successed = false;
		try
		{
			successed = file.createNewFile();
		} 
		catch (IOException e)
		{
			successed = false;
			
			e.printStackTrace();
		}
		
		if (successed)
		{
			Catelog catelog = new Catelog(name, 0);
			
			ArrayList<Catelog> catelogs = readCatelogs(context);
			 
			catelogs.add(catelog);
			
			writeCatelogs(context, catelogs);
		}
		
		return successed;
	}

	// 读取某个列表的所有歌曲
	public static ArrayList<Music> readCatelogItem(Context context, String catelog)
	{
		ArrayList<Music> list = new ArrayList<Music>();
		DataInputStream inData = null;
		try
		{
			File dir = context.getDir(XyzApplication.CATELOG_DIR, Context.MODE_PRIVATE);
			String path = dir.getAbsolutePath() + File.separator + catelog;
			
			InputStream in = new FileInputStream(path);
			inData = new DataInputStream(in);
	
			int count = inData.readInt();
			for (int i = 0; i < count; ++i)
			{
				Music music = new Music();
				
				music.setAlbum(inData.readUTF());
				music.setArtist(inData.readUTF());
				music.setPath(inData.readUTF());
				music.setTitle(inData.readUTF());
				music.setDuration(inData.readInt());
				
				list.add(music);
			}
		}  
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			if (inData != null)
			{
				try
				{
					inData.close();
					
				} catch (IOException e)
				{
					 
					e.printStackTrace();
				}
			}
		}
		return list;
	}

	// 写歌曲到某个列表
	public static void writeCatelogItem(Context context, String catelog, ArrayList<Music> musics)
	{
		DataOutputStream outData = null;
		try
		{
			File dir = context.getDir(XyzApplication.CATELOG_DIR, Context.MODE_PRIVATE);
			File path = new File(dir.getAbsolutePath() + File.separator + catelog);
			
			OutputStream out = new FileOutputStream(path);
			outData = new DataOutputStream(out);
			
			Music music = null;
			outData.writeInt(musics.size());
			for (Iterator<Music> ite = musics.iterator(); ite.hasNext();)
			{
				 music = ite.next();
				 
				 outData.writeUTF(music.getAlbum());
				 outData.writeUTF(music.getArtist());
				 outData.writeUTF(music.getPath());
				 outData.writeUTF(music.getTitle());
				 outData.writeInt(music.getDuration());
				  
			}
			
		} catch (FileNotFoundException e)
		{
			
			e.printStackTrace();
		} catch (IOException e)
		{
			
			e.printStackTrace();
		}
		finally
		{
			if (outData != null)
			{
				try
				{
					outData.close();
					
				} catch (IOException e)
				{
					 
					e.printStackTrace();
				}
			}
		}
	}
	
	// 删除某个列表
	public static void deleteCatelog(Context context, String name)
	{
		// 删除catelog文件
		File dir = context.getDir(XyzApplication.CATELOG_DIR, Context.MODE_PRIVATE);
		String path = dir.getAbsolutePath() + File.separator + name;
		File file = new File(path);
		file.delete();
		
		// 在总的catelog列表中删除
		Catelog catelog = new Catelog(name, 0);
		
		ArrayList<Catelog> catelogs = readCatelogs(context);
		catelogs.remove(catelog);
		
		writeCatelogs(context, catelogs);

		
	}
	
	// 删除多个列表
	public static void deleteCatelogs(Context context, ArrayList<String> strs)
	{
		ArrayList<Catelog> catelogs = readCatelogs(context);
		Catelog catelog = null;
		for (int i = 0; i < strs.size(); ++i)
		{
			catelog = new Catelog(strs.get(i), 0);
			catelogs.remove(catelog);
		}
		writeCatelogs(context, catelogs);
		
		// 删除catelog文件
		File dir = context.getDir(XyzApplication.CATELOG_DIR, Context.MODE_PRIVATE);
		File file = null;
		String path = null;
		for (int i = 0; i < strs.size(); ++i)
		{
			path = dir.getAbsolutePath() + File.separator + strs.get(i);
			file = new File(path);
			file.delete();
		}
	}

	// 显示新建列表对话框
	public static void newCatelogDialog(final Context context)
	{
		AlertDialog.Builder build = new AlertDialog.Builder(context);
		build.setTitle(R.string.new_playlist_dialog);
		final EditText edit = new EditText(context);
		build.setView(edit);
		build.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				String name = edit.getText().toString().trim();
				if ("".equals(name))
				{
					Toast.makeText(context, R.string.playlist_name_is_empty, Toast.LENGTH_LONG).show();
					return;
				}
				
				if (!CatelogUtils.getCatelogsName(context).contains(name))
				{
					CatelogUtils.createCatelog(context, name);
				}
				else
				{
					Toast.makeText(context, R.string.new_playlist_same, Toast.LENGTH_SHORT).show();
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

	// 增加歌曲到某个列表
	public static void addToCatelog(Context context, String catelog, ArrayList<Music> music) 
	{
		// 总列表中对应列表加一
		ArrayList<Catelog> catelogs = readCatelogs(context);
			
		if (catelogs == null)
		{
			return;
		}
		Catelog log = new Catelog(catelog, 0);
		int index = catelogs.indexOf(log);
		log = catelogs.get(index);
		
		// 列表中保存歌曲信息
		ArrayList<Music> musics = readCatelogItem(context, catelog);
		for (int i = 0; i < music.size(); ++i)
		{
			if (!musics.contains(music.get(i)))
			{
				musics.add(music.get(i));
				log.inc();
			}	
		}
		writeCatelogs(context, catelogs);
		writeCatelogItem(context, catelog, musics);
	}
	// 从列表中删除歌曲
	public static void deleteFromCatelog(Context context, String catelogName, Music music)
	{
		ArrayList<Music> musics = CatelogUtils.readCatelogItem(context, catelogName);
		musics.remove(music);
		CatelogUtils.writeCatelogItem(context, catelogName, musics);	
		
		ArrayList<Catelog> catelogs = CatelogUtils.readCatelogs(context);
		Catelog cate = new Catelog(catelogName, 0);
		int index = catelogs.indexOf(cate);
		cate = catelogs.get(index);
		cate.setCount(cate.getCount() - 1);
		CatelogUtils.writeCatelogs(context, catelogs);
	}
	
	public static void deleteFromCatelog(Context context, String catelogName, ArrayList<Music> mu)
	{
		ArrayList<Music> musics = CatelogUtils.readCatelogItem(context, catelogName);
		for (int i = 0; i < mu.size(); ++i)
		{
			musics.remove(mu.get(i));
		}
		CatelogUtils.writeCatelogItem(context, catelogName, musics);	
		
		ArrayList<Catelog> catelogs = CatelogUtils.readCatelogs(context);
		Catelog cate = new Catelog(catelogName, 0);
		int index = catelogs.indexOf(cate);
		cate = catelogs.get(index);
		cate.setCount(cate.getCount() - mu.size());
		CatelogUtils.writeCatelogs(context, catelogs);
	}
	
	// 删除列表中的无效项
	public static void deleteInvalidFromCatelog(Context context, String catelogName)
	{
		ArrayList<Music> musics = CatelogUtils.readCatelogItem(context, catelogName);
		Music music = null;
		File file = null;
		int deleteCount = 0;
		for (Iterator<Music> ite = musics.iterator(); ite.hasNext(); )
		{
			music = ite.next();
			file = new File(music.getPath());
			if (!file.exists())
			{
				ite.remove();
				deleteCount++;
			}
		}
		CatelogUtils.writeCatelogItem(context, catelogName, musics);
		
		ArrayList<Catelog> catelogs = CatelogUtils.readCatelogs(context);
		Catelog catelog = new Catelog(catelogName, 0); 
		int index = catelogs.indexOf(catelog);
		catelog = catelogs.get(index);
		catelog.setCount(catelog.getCount() - deleteCount);
		CatelogUtils.writeCatelogs(context, catelogs);
	}


}
