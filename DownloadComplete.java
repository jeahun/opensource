package com.example.webview;

import java.io.File;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import kr.agworks.sedisk.common.util.Logger;

public class DownloadComplete extends BaseAdapter {
	private static final String TAG = "DownloadListAdapter";
	private ArrayList<Item> items;
	// 첫번째
	private static Context mContext;
	long fileSize;
	Async_SdCard sdCardAsync;
	private ProgressDialog mDlg;
	private onComplteItemClickListener listener;

	public DownloadComplete(Context mContext, ArrayList<Item> items, onComplteItemClickListener listener) {
		this.items = items;
		this.mContext = mContext;
		this.listener = listener;
	}

	public void addItem(Item downFile) {
		Log.d(TAG, "addItem : " + downFile);
		this.items.add(downFile);
		this.notifyDataSetChanged();
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		View v = convertView;

		if (v == null) {
			LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.item_complete, null);
		}
		final TextView currentTxt = (TextView) v.findViewById(R.id.notification);
		final ImageButton btSdcard = (ImageButton) v.findViewById(R.id.sdcard);

		final Item item = items.get(position);
		currentTxt.setText(item.realName);

		btSdcard.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {	
					if (getExternalMemorySize() == 0) {
						Toast.makeText(mContext, "현재 외장메모리에 용량이 부족합니다. 메모리 체크 후 다시 시도해주세요..",
								Toast.LENGTH_SHORT).show();
					}else if (listener != null) {
						listener.sdCard(item);
					// permission체크해주기
					if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
						listener.permissionCheck();
					}
					/*
					 * 사용자의 OS 버전이 마시멜로우 이하일 떄 else { Toast.makeText(mContext,
					 * " 시스템 설정을 통하여 권한을 주시면 작업이 가능합니다.",
					 * Toast.LENGTH_SHORT).show(); }
					 */
				}
			}
		});

		currentTxt.setOnClickListener(new OnClickListener() {

			@SuppressWarnings("static-access")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent Intent = new Intent(android.content.Intent.ACTION_VIEW);
				String extension = android.webkit.MimeTypeMap
						.getFileExtensionFromUrl(Uri.fromFile((File) item.downFile).toString());
				String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
				Intent.setDataAndType(Uri.fromFile((File) item.downFile), mimetype);
				Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(Intent);
				Logger.d(TAG, "Intent" + Intent);
			}
		});

		return v;
	}

	public static String getExternalStorageStatus() {
		String status = Environment.getExternalStorageState();

		return status;
	}

	public static boolean isAvailableExternalStorage() {
		String status = getExternalStorageStatus();

		if (!TextUtils.isEmpty(status) && status.equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	static public boolean externalMemoryAvailable() {
		return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

	}

	/** 사용가능한 외장 메모리 크기를 가져온다 */
	private long getExternalMemorySize() {
		if (isStorage(true) == true) {
			String path = Environment.getExternalStorageDirectory().getPath() + "/external_sd";
			StatFs stat = new StatFs(path);
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;
		} else {
			return -1;
		}
	}

	/** 외장메모리 sdcard 사용가능한지에 대한 여부 판단 */
	private boolean isStorage(boolean requireWriteAccess) {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		} else if (!requireWriteAccess && Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	// // Adapter가 관리할 Data의 개수를 설정 합니다. items에 size만큼.
	@Override
	public int getCount() {
		return items.size();
	}

	// Adapter가 관리하는 Data의 Item 의 Position을 <객체> 형태로 얻어 옵니다.
	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	// Adapter가 관리하는 Data의 Item 의 position 값의 ID 를 얻어 옵니다.
	@Override
	public long getItemId(int position) {
		return position;
	}

	public interface onComplteItemClickListener {
		void sdCard(Item item);

		void isStorage();

		void permissionCheck();

		void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);
	}
}