package com.willing.xyz.adapter;

import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.willing.xyz.R;
import com.willing.xyz.util.CatelogUtils;

public class CatelogAdapter extends SimpleAdapter
{
	public static final String CATELOG_NAME = "catelogName";
	public static final String CATELOG_COUNT = "count";
	
	private LayoutInflater mInflater;
	private Context mContext;
 
	private boolean mActionModeStarted;
	
	public CatelogAdapter(Context context, List<? extends Map<String, ?>> data,
			int resource, String[] from, int[] to, boolean isStarted)
	{
		super(context, data, resource, from, to);
		
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mActionModeStarted = isStarted;
	}

	public void setActionModeStarted(boolean started)
	{
		mActionModeStarted = started;
	}
	public boolean isActionModeStarted()
	{
		return mActionModeStarted;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder = null;
		if (convertView == null)
		{
			convertView = mInflater.inflate(R.layout.catelog_item, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.tv_playlist_name);
			holder.num = (TextView) convertView.findViewById(R.id.tv_playlist_num);
			holder.delete = (ImageButton) convertView.findViewById(R.id.ib_delete_playlist);
			holder.checkbox = (CheckBox) convertView.findViewById(R.id.cb_checked);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		final Map<String, String> map = (Map<String, String>) getItem(position);
		final String name = map.get(CATELOG_NAME);
		holder.name.setText(name);
		holder.num.setText(map.get(CATELOG_COUNT));

		
		if (isActionModeStarted())
		{
			if (holder.checkbox.getVisibility() == View.GONE)
			{
				holder.checkbox.setVisibility(View.VISIBLE);
			}
			ListView listView = (ListView) parent;
			holder.checkbox.setChecked(listView.isItemChecked(position));
		}
		else
		{
			if (holder.checkbox.getVisibility() == View.VISIBLE)
			{
				holder.checkbox.setVisibility(View.GONE);
			}
			holder.delete.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setTitle("È·¶¨É¾³ý " + name + " Âð£¿");
					builder.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							CatelogUtils.deleteCatelog(mContext, name);
						}
					});
					builder.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener()
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
			});
		}
		
		return convertView;
	}
	
	private static class ViewHolder
	{
		TextView name;
		TextView num;
		ImageButton delete;
		CheckBox checkbox;
	}
	
}
