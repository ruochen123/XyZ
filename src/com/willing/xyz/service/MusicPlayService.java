package com.willing.xyz.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import com.willing.xyz.R;
import com.willing.xyz.activity.MainActivity;
import com.willing.xyz.entity.Music;
import com.willing.xyz.receiver.RemoteControlRecevier;

public class MusicPlayService extends Service implements OnAudioFocusChangeListener
{
	private static final int ID_FOREGROUND_NOTIFACTION = 1;
	
	public static final int PLAY_MODE_ORDER = 0;
	public static final int PLAY_MODE_CIRCLE = 1;
	public static final int PLAY_MODE_SINGLE = 2;
	public static final int PLAY_MODE_RANDOM = 3;
	public static final int[] PLAY_MODE = {R.drawable.play_mode_order, R.drawable.play_mode_circle,
		R.drawable.play_mode_single, R.drawable.play_mode_random};
	
	private int mPlayMode;
	
	private ArrayList<Music> mPlayList = new ArrayList<Music>(); // �����б�
	private int mIndex = 0;   // ��ǰ���ŵ�������mPlayList�е�λ��
	
	
	
	public class MusicPlayBinder extends Binder
	{
		public MusicPlayService getService()
		{
			return MusicPlayService.this;
		}
	}
	
	private final MusicPlayBinder mBinder = new MusicPlayBinder();
	private MediaPlayer	mPlayer;
	private boolean isError;
	private AudioManager	mAudioManager;
	private Builder	mNotificationBuilder;
	
	// ������
	LinkedList<MusicPlayerListener> mListener = new LinkedList<MusicPlayerListener>();
	
	// ��ʧȥ����֮ǰ�Ƿ����ڲ���
	private boolean	mBeforeLossIsPlaying;
	// ʧȥ����֮ǰ������
	private int	mBeforeLossVolume;
	// �Ƿ���ʧȥ����ʱ������������
	private boolean	mResotreVolume;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		setupForeground();
	
		initMediaPlayer();
		
	}

	/**
	 * ���ø�ServiceΪǰ̨
	 */
	private void setupForeground()
	{
		// ����Ϊǰ̨����
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
		
		mNotificationBuilder = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle("���� - ����")
			.setContentText("ר��")
			.setContentIntent(pendingIntent);
			
		startForeground(ID_FOREGROUND_NOTIFACTION, mNotificationBuilder.build());
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		mAudioManager.abandonAudioFocus(this);
		
		if (mPlayer != null)
		{
			release();
		}
		
		stopForeground(true);
	}
	
	private void initMediaPlayer()
	{
		mPlayer = new MediaPlayer();
		
		setMediaPlayerListener();
		
	}
	
	private void setMediaPlayerListener()
	{
		mPlayer.setOnCompletionListener(new OnCompletionListener()
		{
			
			@Override
			public void onCompletion(MediaPlayer mp)
			{
				int playMode = getPlayMode();
			
				switch (playMode)
				{
				case PLAY_MODE_CIRCLE:
					mIndex = (mIndex + 1) % mPlayList.size();
					
					break;
				case PLAY_MODE_ORDER:
					if (mIndex < mPlayList.size() - 1)
					{
						mIndex++;
					}
					break;
				case PLAY_MODE_RANDOM:
					mIndex = new Random().nextInt(mPlayList.size() - 1);
					
					break;
				case PLAY_MODE_SINGLE:
					
					break;
				default:
					break;
				}
 
				if (mIndex == mPlayList.size() - 1 && playMode == PLAY_MODE_ORDER)
				{
					mAudioManager.abandonAudioFocus(MusicPlayService.this);
				}
				else
				{
					MusicPlayService.this.playNewMusic();
				}
 
			}
		});
	
		mPlayer.setOnPreparedListener(new OnPreparedListener()
		{
			
			@Override
			public void onPrepared(MediaPlayer mp)
			{
				if (mPlayer != null)
				{
					mPlayer.start();
				}
				
				isError = false; 
			}
		});
		
		mPlayer.setOnErrorListener(new OnErrorListener()
		{
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra)
			{
				isError = true;
				
				return true;
			}
		});
	
	}

	public boolean isPlaying()
	{
		if (mPlayer == null)
		{
			return false;
		}
		boolean flag = false;
		try
		{
			flag = mPlayer.isPlaying();
		}
		catch (IllegalStateException ex)
		{
			flag = false;
		}
		return flag;
	}
	
	public void seekTo(int pos)
	{
		if (isError || mPlayer == null)
		{
			return;
		}
		try
		{
			mPlayer.seekTo(pos);
		}
		catch (IllegalStateException ex)
		{
			init();
			
			mPlayer.seekTo(pos);
		}
	}
	
	public int getCurPos()
	{
		if (mPlayer == null)
		{
			return 0;
		}
		return mPlayer.getCurrentPosition();
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return mBinder;
	}

	public void start()
	{
		if (isError || mPlayer == null)
		{
			return;
		}
		if (!requestFocus())
		{
			return;
		}
		if (mIndex >= mPlayList.size() - 1 || mIndex < 0)
		{
			return;
		}
		try
		{
			mPlayer.start();
			
			fireStartMusicPlay();
		}
		catch (IllegalStateException ex)
		{
			init();
			
			mPlayer.start();
		}
	}
	
	public void pause()
	{
		if (isError || mPlayer == null)
		{
			return;
		}
 
		if (isPlaying())
		{
			mPlayer.pause();
			
			firePauseMusicPlay();
		}
	}
	
	public void playNewMusic()
	{
		init();
		
		Music info = getPlayingMusic();
		if (info != null)
		{
			// �޸�Notification
			mNotificationBuilder.setSmallIcon(R.drawable.ic_launcher)  // ר������
					.setContentTitle(info.getTitle() + " - " + info.getArtist())
					.setContentText(info.getAlbum());
			
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); 
			notificationManager.notify(ID_FOREGROUND_NOTIFACTION, mNotificationBuilder.build());
		}
		fireChangeMusicPlay();
		
		start();
	}
	
	private void init()
	{
		if (!requestFocus())
		{
			return;
		}
		
		Music info = getPlayingMusic();
		if (info != null)
		{
			if (mPlayer == null)
			{
				initMediaPlayer();
			}
			mPlayer.reset();
			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			try
			{
				mPlayer.setDataSource(info.getPath());
				mPlayer.prepareAsync();
			} 
			catch (Exception e)
			{
				isError = true;
			}  
		}
	}

	@SuppressWarnings("deprecation")
	private boolean requestFocus()
	{
		boolean flag =  mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == 
				AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
		
		if (flag)
		{
			mAudioManager.registerMediaButtonEventReceiver(new ComponentName(getApplicationContext(), 
					RemoteControlRecevier.class));
		}
		
		return flag;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onAudioFocusChange(int focusChange)
	{
		switch (focusChange)
		{
		case AudioManager.AUDIOFOCUS_GAIN:
			
			if (mResotreVolume)
			{
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mBeforeLossVolume, 0);
				mResotreVolume = false;
			}

			if (mBeforeLossIsPlaying)
			{
				start();
			}
			
			
			break;
			
		case AudioManager.AUDIOFOCUS_LOSS:
			pause();
			
			// �÷�����ʾע�������������������������󣬷��򲻻��ý���
			mAudioManager.abandonAudioFocus(this);
			
			mAudioManager.unregisterMediaButtonEventReceiver(new ComponentName(getApplicationContext(), 
					RemoteControlRecevier.class));
			
			break;
			
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			if (isPlaying())
			{
				pause();
				mBeforeLossIsPlaying = true;
			}
			else
			{
				mBeforeLossIsPlaying = false;
			}
			break;
			
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			// ��������
			mBeforeLossVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			mResotreVolume = true;
			
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 
					mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / 3, 0);
			
			break;
			
		}
	}

	private void release()
	{
		mPlayer.release();
		mPlayer = null;
	}
	
 
//#start �����б�	
	
	public ArrayList<Music> getPlayList()
	{
		return mPlayList;
	}
	
	public void addToPlayList(Music music, boolean play)
	{
		int index = 0;
		if (!mPlayList.contains(music))
		{
			mPlayList.add(music);
			index = mPlayList.size() - 1;
		}
		else
		{
			index = mPlayList.indexOf(music);
		}
		
		if (play)
		{
			mIndex = index;
			playNewMusic();
		}
	}
	public void addToPlayList(List<Music> musics, boolean clean)
	{
		if (musics == null || musics.size() == 0)
		{
			return;
		}
		if (clean)
		{
			mPlayList.clear();
		}
		
		for (Iterator<Music> ite = musics.iterator(); ite.hasNext(); )
		{
			addToPlayList(ite.next(), false);
		}
		mIndex = mPlayList.indexOf(musics.get(0));
		playNewMusic();
	}
	
	public Music getPlayingMusic()
	{
		Music info = null;
		try
		{
			info = mPlayList.get(mIndex);
		}
		catch (Exception ex)
		{
			info = null;
		}
		return info;
	}

	public void next()
	{
		if (mPlayList.size() == 0)
		{
			return;
		}
		mIndex = (mIndex + 1) % mPlayList.size();
		playNewMusic();
	}
	
	public void pre()
	{
		if (mIndex == 0)
		{
			mIndex = mPlayList.size() - 1;
		}
		else
		{
			mIndex--;
		}
		playNewMusic();
	}
	
	public void setIndex(int index)
	{
		if (index < 0 || index >= mPlayList.size())
		{
			return;
		}
		else
		{
			mIndex = index;
		}
	}
//#end	
	

//#start PlayMode
	
	public int getPlayMode()
	{
		return mPlayMode;
	}
	
	public int nextPlayMode()
	{
		mPlayMode = (mPlayMode + 1) % PLAY_MODE.length;
		
		return mPlayMode;
	}
	
	
//#end
	
	public void registerMusicPlayerListener(MusicPlayerListener listener)
	{
		mListener.add(listener);
	}
	
	public void unregisterMusicPlayerListener(MusicPlayerListener listener)
	{
		mListener.remove(listener);
	}
	
	public void firePauseMusicPlay()
	{
		for (Iterator<MusicPlayerListener> ite = mListener.iterator(); ite.hasNext(); )
		{
			MusicPlayerListener listener = ite.next();
			
			listener.pause();
		}
	}
	
	public void fireStartMusicPlay()
	{
		for (Iterator<MusicPlayerListener> ite = mListener.iterator(); ite.hasNext(); )
		{
			MusicPlayerListener listener = ite.next();
			
			listener.start();
		}
	}
	
	public void fireChangeMusicPlay()
	{
		for (Iterator<MusicPlayerListener> ite = mListener.iterator(); ite.hasNext(); )
		{
			MusicPlayerListener listener = ite.next();
			
			listener.changeMusic();
		}
	}
	
	public static interface MusicPlayerListener
	{
		public void pause();
		public void start();
		// ��������
		public void changeMusic();
	}
	
}
