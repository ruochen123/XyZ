package com.willing.xyz.view;

 

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import com.willing.xyz.R;
import com.willing.xyz.entity.LrcEntry;
import com.willing.xyz.entity.Lyric;
import com.willing.xyz.util.LrcParser;

/**
 * Created by Willing on 2015/10/15 0015.
 */
public class LrcView extends View
{
    private static final int VELOCITY = 1000;

    private static final String NO_LYRIC = "无歌词信息";

    private Paint mPaint;
    private int mFontHeight;
    private int mLeading = 2; // 行间距
    private int mCurLrcColor = 0xff0000;


    private Scroller mScroller;
    private VelocityTracker mTracker;
    
    private boolean mIsScrolled;
    private volatile boolean mIsOverTime;
    private int mDownX;
    private int mDownY;
    
    
    private Lyric mLyric;
    private int mCurTime; //
    private int mTimeIndex;
    private String mPath;

	private Thread	mLrcCenterTask;

    

    public LrcView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LrcView(Context context) {
        this(context, null, 0);
    }

    public LrcView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        TypedArray arr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.lrcView, defStyleAttr, 0);
        
        mLeading = (int) arr.getDimension(R.styleable.lrcView_leading, 2);

        mCurLrcColor = arr.getColor(R.styleable.lrcView_curLrcColor, 0xff0000);
        // 初始化
        mPaint = new Paint();
        mPaint.setTextSize(arr.getDimension(R.styleable.lrcView_textSize, 20));
        mPaint.setColor(arr.getColor(R.styleable.lrcView_textColor, 0));
        mPaint.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetricsInt fontMetricsInt = mPaint.getFontMetricsInt();
        mFontHeight = fontMetricsInt.descent - fontMetricsInt.ascent + fontMetricsInt.leading + mLeading;

        mScroller = new Scroller(context);

        
    }

    public void setPath(String path)
    {
        mPath = path;
        mLyric = null;
        
        postInvalidate();
    }
    public String getPath()
    {
    	return mPath;
    }
    
    public Lyric getLyric()
    {
        return mLyric;
    }

    // 毫秒
    public void setCurTime(int time)
    {
    	time = getTimeFromTime(time);
        if (time != mCurTime)
        {
            mCurTime = time;
            postInvalidate();
        }
    }
    public int getCurTime()
    {
        return mCurTime;
    }

    // 必须在设置歌词前调用
    public void setTextSize(float size)
    {
    	mPaint.setTextSize(size);
        Paint.FontMetricsInt fontMetricsInt = mPaint.getFontMetricsInt();
        mFontHeight = fontMetricsInt.descent - fontMetricsInt.ascent + fontMetricsInt.leading + mLeading;

    }
    public float getTextSize()
    {
    	return mPaint.getTextSize();
    }
    
    public void setTextColor(int color)
    {
    	mPaint.setColor(color);
    }
    public int getTextColor()
    {
    	return mPaint.getColor();
    }

    public void setCurLrcColor(int color)
    {
        mCurLrcColor = color;
    }

    public int getCurLrcColor()
    {
        return mCurLrcColor;
    }

    public int getLeading()
	{
		return mLeading;
	}

    // 必须在设置歌词前调用
	public void setLeading(int leading)
	{
		mLeading = leading;
	}

	void setOverTime(boolean time)
	{
		mIsOverTime = time;
	}



	@Override
    protected void onDraw(Canvas canvas)
    {
        if (mLyric == null)
        {
            mLyric = LrcParser.parse(mPath, getWidth() - getPaddingLeft() - getPaddingRight(), mPaint);
        }
		int y = mFontHeight - mPaint.getFontMetricsInt().descent;
		int x = (getWidth() - getPaddingLeft() - getPaddingRight()) / 2;

        // 没有歌词时
        if (mLyric == null || mLyric.getLrcs().size() == 0)
        {
            Rect bounds = new Rect();
            mPaint.getTextBounds(NO_LYRIC, 0, NO_LYRIC.length(), bounds);


            y = (getHeight() - getPaddingTop() - getPaddingBottom() - bounds.height()) / 2 + -mPaint.getFontMetricsInt().ascent;

            canvas.drawText(NO_LYRIC, 0, NO_LYRIC.length(), x, y, mPaint);

            return;
        }
 


        
        int drawTime = (getHeight() - getPaddingTop() - getPaddingBottom()) / mFontHeight;

        int lines = 0;

        int i = getScrollY() / mFontHeight;
        i = Math.max(0, i);
        
        if ((mIsScrolled && mIsOverTime) || !mIsScrolled)
        {
        	i = mTimeIndex - drawTime / 2;
        	i = Math.max(0, i);
        	
            mIsScrolled = false;
            mIsOverTime = false;
        }
        
        for (; i < mLyric.getLrcs().size(); ++i)
        {
            LrcEntry line = mLyric.getLrcs().get(i);
            
            if (line.getTime() == mCurTime)
            {
                int savedColor = mPaint.getColor();
                mPaint.setColor(mCurLrcColor);
                canvas.drawText(line.getLine(), 0, line.getLine().length(), x, y + getScrollY() + getPaddingTop(), mPaint);
                mPaint.setColor(savedColor);
            }
            else
            {
                canvas.drawText(line.getLine(), 0, line.getLine().length(), x, y + getScrollY() + getPaddingTop(), mPaint);
            }
            y += mFontHeight;

            if (lines >= drawTime)
            {
                break;
            }
        }
 
    }

    private int getTimeFromTime(int time)
    {
    	if (mLyric == null)
    	{
    		return 0;
    	}
        int lastFoundIndex = -1;
        int begin = 0;
        int end = mLyric.getLrcs().size() - 1;

        int mid = 0;
        while (begin <= end)
        {
            mid = (begin + end) / 2;
 
            if (time == mLyric.getLrcs().get(mid).getTime() + mLyric.getOffset())
            {
            	lastFoundIndex = mid;
                end = mid - 1;
            }
            else if (time > mLyric.getLrcs().get(mid).getTime() + mLyric.getOffset())
            {
                begin = mid + 1;
            }
            else
            {
                end = mid - 1;
            }
        }
        
        if (lastFoundIndex == -1)
        {
        	lastFoundIndex = Math.min(begin, mLyric.getLrcs().size() - 1);
        }
        while (lastFoundIndex > 0 && mLyric.getLrcs().get(lastFoundIndex).getTime() > time)
        {
        	lastFoundIndex--;
        }
        mTimeIndex = lastFoundIndex;
        
        return mLyric.getLrcs().get(lastFoundIndex).getTime();
    }

    @Override
    public void computeScroll()
    {
        if (mScroller.computeScrollOffset())
        {
            scrollTo(0, mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int action = event.getAction();
        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
            	
            	 
            	mDownY = (int) event.getY();
            	
                if (mTracker == null)
                {
                    mTracker = VelocityTracker.obtain();
                }
                else
                {
                    mTracker.clear();
                }
                mTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_MOVE:
                mTracker.addMovement(event);

                mIsScrolled = true;
                mIsOverTime = false;

             
                
                scrollBy(0 , -(int) (event.getY() - mDownY));
                postInvalidate();
                mDownY = (int) event.getY();
                
                break;
            case MotionEvent.ACTION_UP:
                mTracker.addMovement(event);
                mTracker.computeCurrentVelocity(VELOCITY, ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity());
 
                if (mLyric == null)
                {
                	break;
                }
                if (mTracker.getYVelocity() >= ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity())
                {
	                mScroller.fling(0, mScroller.getCurrY(), 0, -(int)mTracker.getYVelocity(), 0, 0, 0,
	                        mLyric.getLrcs().size() * mFontHeight - getHeight() + getPaddingBottom() + mFontHeight);

                }

                if (mLrcCenterTask != null)
                {
                	mLrcCenterTask.interrupt();
                }
                mLrcCenterTask = new LrcCenterTask(this);
                mLrcCenterTask.start();
                
                mTracker.recycle();
                mTracker = null;
                postInvalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            	mTracker.recycle();
            	mTracker = null;
            	break;
        }
        return true;
    }
    
    static class LrcCenterTask extends Thread
    { 
    	private LrcView	mView;

		public LrcCenterTask(LrcView view)
    	{
    		mView = view;
    	}
    	
    	@Override
    	public void run()
    	{
    		try
			{
				Thread.sleep(5000);
				
				if (mView != null)
				{
					mView.setOverTime(true);
				}
				
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
    	}
         
    }
}

