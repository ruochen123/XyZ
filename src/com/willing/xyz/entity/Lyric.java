package com.willing.xyz.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 姝岃瘝鐨勮〃绀�
 * Created by Willing on 2015/11/4 0004.
 */
public class Lyric
{
    private String mAlbum; // 鎵�灞炰笓杈�
    private String mArtist; // 鍒涗綔璇ユ瓕璇嶇殑浣滆��
    private String mBy;    // 璇ユ瓕璇嶆枃浠剁敱璋佸埗浣�
    private int mOffset;   // 鏁翠釜姝岃瘝鏂囦欢鐨勫亸绉诲��
    private String mSoft;  // 鍒朵綔璇ユ瓕璇嶇殑杞欢
    private String mVersion; // 杞欢鐗堟湰
    private String mTitle; // 姝岃瘝鏍囬
    private int mLength; // 姝屾洸闀垮害
    private String mAuthor; // don't know.


    private ArrayList<LrcEntry> mLines;


    public Lyric()
    {
        mLines = new ArrayList<>();
    }

    public List<LrcEntry> getLrcs()
    {
        return mLines;
    }

    public void addLrc(int time, String line)
    {
        mLines.add(new LrcEntry(time, line));
    }

    public String getAlbum()
    {
        return mAlbum;
    }

    public void setAlbum(String album)
    {
        mAlbum = album;
    }

    public String getArtist()
    {
        return mArtist;
    }

    public void setArtist(String artist)
    {
        mArtist = artist;
    }

    public String getBy()
    {
        return mBy;
    }

    public void setBy(String by)
    {
        mBy = by;
    }

    public int getOffset()
    {
        return mOffset;
    }

    public void setOffset(int offset)
    {
        mOffset = offset;
    }

    public String getSoft()
    {
        return mSoft;
    }

    public void setSoft(String soft)
    {
        mSoft = soft;
    }

    public String getTitle()
    {
        return mTitle;
    }

    public void setTitle(String title)
    {
        mTitle = title;
    }

    public String getVersion()
    {
        return mVersion;
    }

    public void setVersion(String version)
    {
        mVersion = version;
    }

    public int getLength()
    {
        return mLength;
    }

    public void setLength(int length)
    {
        mLength = length;
    }

    public String getAuthor()
    {
        return mAuthor;
    }

    public void setAuthor(String author)
    {
        mAuthor = author;
    }
    
    @Override
    public String toString()
    {
    	StringBuilder builder = new StringBuilder();
    	for (int i = 0; i < mLines.size(); ++i)
    	{
    		builder.append("Time: " + mLines.get(i).getTime() + " - " + mLines.get(i).getLine());
    		builder.append('\n');
    	}
    	return builder.toString();
    }
}

