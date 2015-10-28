package com.willing.xyz.adapter;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.willing.xyz.R;

public class SingerAdapter extends SimpleAdapter
{

	public static final String	SINGER_NUM	= "singerNum";
	public static final String	SINGER_NAME	= "singerName";
	
	
	private LayoutInflater mInflater;
	private Context mContext;
 
	public SingerAdapter(Context context, List<? extends Map<String, ?>> data,
			int resource, String[] from, int[] to)
	{
		super(context, data, resource, from, to);
		
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder = null;
		if (convertView == null)
		{
			convertView = mInflater.inflate(R.layout.singer_item, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.tv_singer_name);
			holder.num = (TextView) convertView.findViewById(R.id.tv_singer_num);
			
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		
		final Map<String, String> map = (Map<String, String>) getItem(position);
		holder.name.setText(map.get(SINGER_NAME)); 
		holder.num.setText(map.get(SINGER_NUM));
 
		return convertView;
	}
	
	private static class ViewHolder
	{
		TextView name;
		TextView num;
	}
}
