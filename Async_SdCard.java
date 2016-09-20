package com.example.webview;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.widget.Toast;
import kr.agworks.sedisk.common.util.Logger;

public class Async_SdCard extends AsyncTask<Void, Integer, Boolean> {
	private static final String TAG = "SdCardAsync";
	private File originFile;
	private static Context mContext;
	private Item mItem;
	private ProgressDialog mDlg;
	public static String root = null;
	private final String rootFolderName = "/sedisk_DownFile";
	private String downloadFile = "Sedisk_DownloadFile";
	int totalCount;
	int currentCount;
	private File outputFile;

	public Async_SdCard(Context context, Item item) {
		this.mItem = item;
		this.mContext = context;
		this.originFile = mItem.downFile;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Logger.d(TAG, "onPreExecute : ");
		mDlg = new ProgressDialog(mContext);
		mDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mDlg.setButton(DialogInterface.BUTTON_POSITIVE, "취소", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(mContext, "취소처리 되었습니다.", Toast.LENGTH_SHORT).show();

			}
		});
		mDlg.setMessage("외장메모리 파일 업로드시작");

		if (originFile.isDirectory()) {
			totalCount = originFile.listFiles().length;
		} else {
			totalCount = 1;
		}
		currentCount = 0;

		mDlg.setMax(totalCount);

		Logger.d(TAG, "total count : " + totalCount);
		Logger.d(TAG, "originFile : " + originFile);
		Logger.d(TAG, "mDlg.setMessage :");
		mDlg.show();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		// FileInputStream inputStream = null;
		// FileOutputStream outputStream = null;

		// 도착 파일 패스
		File fileList = Environment.getExternalStorageDirectory();
		String outroot = fileList.getAbsolutePath();
		File destDir = new File(outroot + "/" + "/external_sd" + rootFolderName + "/" + "초 웃긴사진모음집");
		// File destDir = new File("/storage/9016-4EF8" + rootFolderName + "/" +
		// "초 웃긴사진모음집");
		Log.d(TAG, "dest dir path : " + destDir.getPath());

		if (totalCount == 1) {
			outputFile = new File(destDir, originFile.getName());
			boolean result = copyFile(originFile, outputFile);
			if (result) {
				currentCount++;
			} else {
				return false;
			}
			publishProgress(currentCount);

		} else {
			if (originFile.exists() && originFile.isDirectory()) {
				for (File originFile : originFile.listFiles()) {
					outputFile = new File(destDir, originFile.getName());
					boolean result = copyFile(originFile, outputFile);

					if (result) {
						currentCount++;
					} else {
						return false;
					}
					publishProgress(currentCount);
				}
			}
		}
		return true;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate();
		mDlg.setProgress(progress[0]);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		Logger.d(TAG, "result : " + result);
		mDlg.dismiss();
		if (result) {
			// fileDelete(originFile);
			Toast.makeText(mContext, "" + totalCount + "개의 작업 완료 되었습니다.", Toast.LENGTH_SHORT).show();
		} else {
			if (getExternalMemorySize() <= getDirSize(originFile)) {
				Logger.d(TAG, "out : " + outputFile);
				Toast.makeText(mContext, "다운로드중 메모리 용량이 가득차버렸습니다. 용량을 비워 주세요.", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, "외장메모리 이동에 실패하였습니다.", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private boolean copyFile(File inputFile, File outputFile) {
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		boolean result = true;

		try {

			if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
				outputFile.getParentFile().mkdirs();
			}
			inputStream = new FileInputStream(inputFile);
			outputStream = new FileOutputStream(outputFile);

			FileChannel fCin = inputStream.getChannel();
			FileChannel fCout = outputStream.getChannel();

			long size = fCin.size();
			fCin.transferTo(0, size, fCout);

			ByteBuffer buf = ByteBuffer.allocateDirect(1024);

			while (true) {
				if (fCin.read(buf) == -1)
					break;
				buf.flip();
				fCout.write(buf);
				buf.clear();
				// fileDelete(input);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "copy File err.", e);
			result = false;
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (Exception e) {
			}
		}

		return result;
	}

	private void fileDelete(File deleteFile) {
		Logger.d(TAG, "delete path : " + deleteFile);
		if (deleteFile != null && deleteFile.exists()) {
			deleteFile.delete();
		}
	}

	private static void removeDir(String outputFile) {
		Logger.d(TAG, "outputFile" + outputFile);
		File file = new File(outputFile);
		File[] childFileList = file.listFiles();
		for (File childFile : childFileList) {
			if (childFile.isDirectory()) {
				removeDir(childFile.getAbsolutePath()); // 하위 디렉토리
			} else {
				childFile.delete(); // 하위 파일
			}
		}

		file.delete(); // root 삭제
	}

	public List<File> getDirFileList(String dirOriginFile) {
		{
			// 디렉토리 파일 리스트
			List<File> dirFileList = null;

			// 파일 목록을 요청한 디렉토리를 가지고 파일 객체를 생성함
			File dir = new File(dirOriginFile);

			// 디렉토리가 존재한다면
			if (dir.exists()) {
				// 파일 목록을 구함
				File[] files = dir.listFiles();

				// 파일 배열을 파일 리스트로 변화함
				dirFileList = Arrays.asList(files);
			}

			return dirFileList;
		}
	}

	/** 사용가능한 외장 메모리 크기를 가져온다 */
	private long getExternalMemorySize() {
		if (isStorage(true) == true) {
			String path = Environment.getExternalStorageDirectory().getPath() + "/external_sd";
			StatFs stat = new StatFs(path);
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			Logger.d(TAG, "size : " + availableBlocks * blockSize);
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

	public long getDirSize(File originFile) {
		long length = 0;
		if (originFile != null && originFile.isDirectory()) {
			for (File file : originFile.listFiles()) {
				if (file.isFile()) {
					length += file.length();
				} else {
					length += getDirSize(file);
				}
			}
		}
		return length;
	}

}