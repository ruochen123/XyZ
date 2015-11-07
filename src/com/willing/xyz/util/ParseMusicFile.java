package com.willing.xyz.util;

import java.io.File;
import java.io.IOException;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import com.willing.xyz.entity.Music;

public class ParseMusicFile
{
	// ���������ļ��е���Ϣ������׳��쳣�����ʾ���ص�Music�ڲ�����Ϣ����Ч�ġ�
	public static Music parse(File file) throws CannotReadException, IOException, TagException, InvalidAudioFrameException
	{
		Music music = new Music();
		
		AudioFile audioFile = null;
		try
		{
			audioFile = AudioFileIO.read(file);
		} 
		catch (ReadOnlyFileException e)
		{
			// continue;
		}
		
		Tag tag = audioFile.getTag();
		if (tag != null)
		{
			try
			{
				music.setTitle(tag.getFirst(FieldKey.TITLE));
				music.setAlbum(tag.getFirst(FieldKey.ALBUM));
				music.setArtist(tag.getFirst(FieldKey.ARTIST));
			}
			catch (KeyNotFoundException ex)
			{
				// continue;
			}
		}
		
		// ��ȡ������ʱ��
		AudioHeader header = audioFile.getAudioHeader();
		int length = header.getTrackLength();
		music.setDuration(length);
		
		music.setPath(file.getCanonicalPath());
		
		return music;
	}

}
