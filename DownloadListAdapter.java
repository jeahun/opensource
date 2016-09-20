package com.example.webview;

import java.util.ArrayList;

import com.example.webview.Async_httpurl.downloadCompleteListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import kr.agworks.sedisk.common.util.Logger;
import kr.agworks.sedisk.common.util.ToastUtil;

public class DownloadListAdapter extends BaseAdapter {

	private static final String TAG = "DownloadListAdapter";
	private ArrayList<Item> items;
	// 첫번째
	private Context mContext;
	private downloadCompleteListener listener;
	long fileSize;
	Async_httpurl helloSync;
	
	public DownloadListAdapter(Context context, downloadCompleteListener listener) {
		this.items = new ArrayList<Item>();
		this.mContext = context;
		this.listener = listener;
	}

	/**
	 * 리스트 추가
	 */
	public void addItems(ArrayList<Item> info) {
		this.items.addAll(info);
		this.notifyDataSetChanged();
	}
	
	public void addItem(Item item) {
		this.items.add(item);
		this.notifyDataSetChanged();
	}
	
	
	/**
	 *  getView 메서드는 실제로 데이터 값이 각 row에 실제로 동작하는 row와 view를 담당하는 곳이다.
	 *  
	 */
	@SuppressWarnings("unused")
	public View getView(final int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.item_file, null);
		}

		final TextView currentTxt = (TextView) v.findViewById(R.id.notification);
		final ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progressbar);
		final TextView persent_a = (TextView) v.findViewById(R.id.persent_a);
		
		final Item item = items.get(position);
		
		
		currentTxt.setText(item.realName);
		Logger.d(TAG, "fileName : " + item.realName);
		helloSync = new Async_httpurl(mContext, listener, item);
		helloSync.setTextView(currentTxt);
		helloSync.setTextView1(persent_a);
		helloSync.setProgressBar(progressBar);
		helloSync.execute();
		
		/**
		 *  start 버튼을 클릭시 httpurlAsync에서 텍스트와 프로그레스바를 가져왔으며,
		 * execute를 적용하여 AsyncTask에서 doInbackground를 실행 하도록 하였습니다.
		 */

		
		ImageButton start = (ImageButton) v.findViewById(R.id.start);
		start.setTag(position);
		start.setOnClickListener(new Button.OnClickListener() {
			

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				helloSync = new Async_httpurl(mContext, listener, item);
				helloSync.setTextView(currentTxt);
				helloSync.setTextView1(persent_a);
				helloSync.setProgressBar(progressBar);
				if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
					helloSync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					} else {
						helloSync.execute();
					}
			}
		});
		ImageButton stop = (ImageButton) v.findViewById(R.id.stop);
		stop.setTag(position);
		stop.setOnClickListener(new Button.OnClickListener() {
		
			
			@Override
			public void onClick(View v) {
				
				currentTxt.setText("다운로드가 중지 되었습니다.");
				helloSync.cancel(true);
			}
		});
		
		//cancel 버튼을 클릭시 디렉토리를 찾아서 삭제 할수 있도록 적용 하였습니다.
		ImageButton cancel = (ImageButton) v.findViewById(R.id.cancel);
		cancel.setTag(position);
		cancel.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(TAG, "DownloadListAdapter : onClick--->>>>>>");
				
				removeItem(item);
				}
		});
		return v;
	}
	
	public void removeItem(Item item) {
		items.remove(item);
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}