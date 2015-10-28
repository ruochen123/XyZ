package com.willing.xyz.adapter;
 
import java.util.LinkedList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;

@SuppressWarnings("deprecation")
public class ViewPagerAdapter extends FragmentPagerAdapter implements OnPageChangeListener, TabListener
{
	private ViewPager mViewPager;
	private ActionBar mActionBar;
	 
	private List<Fragment> fragmentLists = new LinkedList<Fragment>();
	
	public ViewPagerAdapter(FragmentManager fragmentManager, ViewPager viewPager, ActionBar actionBar)
	{
		super(fragmentManager);
		
		mViewPager = viewPager;
		mActionBar = actionBar;
	}
	
	public void addFragment(Fragment frag)
	{
		fragmentLists.add(frag);
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction tran)
	{
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft)
	{
		mViewPager.setCurrentItem(tab.getPosition(), true);
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1)
	{
	}

	@Override
	public void onPageScrollStateChanged(int arg0)
	{
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2)
	{
	}

	@Override
	public void onPageSelected(int position)
	{
		mActionBar.setSelectedNavigationItem(position);
	}

	@Override
	public Fragment getItem(int position)
	{
		return fragmentLists.get(position);
	}

	@Override
	public int getCount()
	{
		return fragmentLists.size();
	}
	
 
	
}
