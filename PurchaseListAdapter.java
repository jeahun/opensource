package com.example.webview;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PurchaseListAdapter extends BaseAdapter {
	private static final String TAG = "PurchaseListAdapter";
	private ArrayList<Item> mListItem;
	private Activity mActivity;
	
	public PurchaseListAdapter(Activity activity, ArrayList<Item> listItem) {
		this.mActivity = activity;
		this.mListItem = listItem;
	}

	@Override
	public int getCount() {
		if (mListItem == null) {
			return 0;
		} else {
			return mListItem.size();
		}
	}

	@Override
	public Object getItem(int position) {
		return mListItem.get(position);
	}

	@Override
	public long getItemId(int position) {
		return (long)position;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = new ViewHolder();
		if (convertView == null) {
			convertView = LayoutInflater.from(mActivity).inflate(R.layout.item_purchase_list, parent, false);
			holder.rlRoot = (RelativeLayout)convertView.findViewById(R.id.rl_root);
			holder.cbItem = (CheckBox)convertView.findViewById(R.id.cb_item);
			holder.tvItem = (TextView)convertView.findViewById(R.id.tv_title);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		final Item dto = mListItem.get(position);
		holder.tvItem.setText(dto.realName);
		
		holder.cbItem.setChecked(dto.isChekced);
		
		holder.rlRoot.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dto.isChekced = !dto.isChekced;
				notifyDataSetChanged();
			}
		});
		
		
		return convertView;
	}
	
	private class ViewHolder {
		RelativeLayout rlRoot;
		TextView tvItem;
		CheckBox cbItem;
	}

}
