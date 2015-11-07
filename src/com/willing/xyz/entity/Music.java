package com.willing.xyz.entity;


public class Music
{
	private String mTitle;
	private String mAlbum;
	private String mArtist;
	private String mPath;
	private int mDuration;
	
	
	public Music()
	{
		mTitle = "δ֪";
		mAlbum = "δ֪";
		mArtist = "δ֪";
		mPath = "";
		mDuration = 0;
	}
	
	public Music(String path)
	{
		mPath = path;
	}
	public Music(String title, String album, String artists, String path,
			int duration)
	{
		mTitle = getString(title);
		mAlbum = getString(album);
		mArtist = getString(artists);
		mPath = path;
		mDuration = duration;
	}
	
	
	public String getTitle()
	{
		return mTitle;
	}
	public void setTitle(String title)
	{
		mTitle = getString(title);
	}
	public String getAlbum()
	{
		return mAlbum;
	}
	public void setAlbum(String album)
	{
		mAlbum = getString(album);
	}
	public String getArtist()
	{
		return mArtist;
	}
	public void setArtist(String artists)
	{
		mArtist = getString(artists);
	}
	public String getPath()
	{
		return mPath;
	}
	public void setPath(String path)
	{
		mPath = path;
	}
	public int getDuration()
	{
		return mDuration;
	}
	public void setDuration(int duration)
	{
		mDuration = duration;
	}
	
	public String getString(String str)
	{
		if (str.trim() == "")
		{
			return "δ֪";
		}
		return str;
	}

	@Override
	public boolean equals(Object o)
	{
		
		if (o == null)
		{
			return false;
		}
		if (o == this)
		{
			return true;
		}
		if (o instanceof Music)
		{
			return ((Music) o).getPath().equals(this.getPath());
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return getPath().hashCode();
	}
	

}
