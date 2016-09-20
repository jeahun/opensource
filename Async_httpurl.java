package com.example.webview;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import kr.agworks.sedisk.common.util.Logger;

public class Async_httpurl extends AsyncTask<Void, String, Void> {
	private static final String TAG = "httpurlAsync";
	private WeakReference<TextView> mTextViewWeakReference;
	private WeakReference<TextView> mTextViewMov;
	private WeakReference<ProgressBar> mProgressBar;
	private Item mItem;
	private downloadCompleteListener listener;
	private File downFile;
	private Context mContext;
	long fileSize, remains = 0;
	long lenghtOfFile;

	View view = null;

	public Async_httpurl(Context context, downloadCompleteListener listener, Item item) {
		this.listener = listener;
		this.mContext = context;
		this.mItem = item;

		this.downFile = mItem.downFile;

	}

	public void setTextView1(TextView textView) {
		mTextViewMov = new WeakReference<TextView>(textView);
	}

	public void setTextView(TextView textView) {
		mTextViewWeakReference = new WeakReference<TextView>(textView);
	}

	public void setProgressBar(ProgressBar progressBar) {
		mProgressBar = new WeakReference<ProgressBar>(progressBar);
	}

	@Override
	protected Void doInBackground(Void... params) {
		int count = 0;
		Log.d(TAG, "doInBackground. url : " + mItem.url);
		Log.d(TAG, "doInBackground. realName : " + mItem.realName);
		/**
		 * getExternalStorageDirectory 파일경로 조사
		 */

		try {
			RandomAccessFile output = new RandomAccessFile(downFile.getAbsolutePath(), "rw");
			fileSize = output.length(); 
			output.seek(fileSize); 
			URL lenUrl = new URL(mItem.url);

			Log.d(TAG, "fileSize : " + fileSize);

			HttpURLConnection conn = (HttpURLConnection) lenUrl.openConnection();
			conn.setRequestProperty("Range", "bytes=" + String.valueOf(fileSize) + '-');
			conn.connect(); // 서버 연결
			InputStream input = conn.getInputStream();
			long remains = conn.getContentLength();
			Log.d(TAG, "remains : " + remains);
			lenghtOfFile = remains + fileSize; 
			byte[] data = new byte[1024];

			while ((count = input.read(data)) != -1) {

				boolean cancelled = isCancelled();
				if (cancelled == false) {
					fileSize += count;
					publishProgress(("" + (int) ((fileSize * 100) / lenghtOfFile)));
					output.write(data, 0, count);
				} else if (cancelled == true) {
					break;
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onProgressUpdate(String... progress) {
		super.onProgressUpdate(progress);
		if (downFile == null) {
			mProgressBar.get().setProgress(0);
		} else {
			mProgressBar.get().setProgress(Integer.parseInt(progress[0]));
			mTextViewWeakReference.get().setText("다운로드중입니다...");
			long downloadingSizeByte = lenghtOfFile;
			long downloadingSizeKbytes = downloadingSizeByte / 1024;
			long downloadingSizeMbytes = downloadingSizeKbytes / 1024;
			long loadingSizeByte = fileSize;
			long loadingSizeKbytes = loadingSizeByte / 1024;
			long loadingSizeMbytes = loadingSizeKbytes / 1024;
			mTextViewMov.get().setText(
					loadingSizeMbytes + " MB/" + downloadingSizeMbytes + " MB" + "(" + progress[0] + ")" + "%");
		}
	}

	@Override
	protected void onCancelled() {
		// TODO Auto-generated method stub
		Log.d(TAG, "OnCancelled : ");
		super.onCancelled();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		// TODO Auto-generated method stub
		Log.d(TAG, "onPreExecute : ");
	}

	@Override
	protected void onPostExecute(Void result) {
		Log.d(TAG, "Post Execute");
		if (lenghtOfFile == 0) {
			mTextViewWeakReference.get().setText("이미 다운로드가 완료된 파일입니다.");
			Toast.makeText(mContext, "이미 다운로드가 완료된 파일입니다", Toast.LENGTH_SHORT).show();

			Log.d(TAG, "FileName" + downFile);
		} else if (lenghtOfFile == fileSize) {
			mProgressBar.get().setProgress(0);
			mTextViewWeakReference.get().setText("다운로드가 완료되었습니다.");
			Toast.makeText(mContext, "다운로드가 완료되었습니다.", Toast.LENGTH_SHORT).show();
			if (this.listener != null) {
				listener.onComplete(mItem);

			}
		}
	}

	public interface downloadCompleteListener {
		void onComplete(Item item);
	}

}