package com.example.webview;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.widget.Toast;
import kr.agworks.sedisk.common.util.Logger;

public class Async_SdCard_tst extends AsyncTask<Void, Integer, Boolean> {
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
	
	private long originFileSize = 0;

	public Async_SdCard_tst(Context context, Item item) {
		this.mItem = item;
		this.mContext = context;
		this.originFile = mItem.downFile;
		this.originFileSize = 0;
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
		File destDir = new File(outroot + "/" + "/external_sd" + rootFolderName + "/");
		Log.d(TAG, "dest dir path : " + destDir.getPath());
		
		boolean result = copyFileOrDir(originFile, destDir);
		
		return result;
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

		if (result) {
			// fileDelete(originFile);
			mDlg.dismiss();
			Toast.makeText(mContext, "" + totalCount + "개의 작업 완료 되었습니다.", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(mContext, "외장메모리 이동에 실패하였습니다.", Toast.LENGTH_SHORT).show();
		}
	}

	private boolean copyFileOrDir(File originSource, File destSource) {
		Logger.d(TAG, "copyFileOrDir.");
		Logger.d(TAG, "originsource : " + originSource.getPath());
		Logger.d(TAG, "destSource : " + destSource.getPath());
		boolean result = false;
		if (originSource.isDirectory()) {
			result &= copyDir(originSource, destSource);
		} else {
			if (destSource != null && !destSource.exists()) {
				destSource.mkdirs();
			}
			result &= copyFile(originSource, destSource);
		}
		
		return result;
	}
	
	private boolean copyDir(File originDir, File destDir) {
		Logger.d(TAG, "copyDir.");
		Logger.d(TAG, "originDir : " + originDir.getPath());
		Logger.d(TAG, "destDir : " + destDir.getPath());
		boolean result = false;
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		
		for (File file : destDir.listFiles()) {
			File destFile = new File(destDir.getPath() + File.separator + file.getName());
			if (destFile.isDirectory()) {
				result &= copyDir(file, destFile);
			} else {
				result &= copyFile(file, destFile);
			}
		}
		
		return result;
	}
	
	private boolean copyFile(File inputFile, File outputFile) {
		
		Logger.d(TAG, "copyFile.");
		Logger.d(TAG, "inputFile : " + inputFile.getPath());
		Logger.d(TAG, "outputFile : " + outputFile.getPath());
		
		boolean result = true;
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
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
			if(getExternalMemorySize() == 0){
				
			}
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
		currentCount++;
		publishProgress(currentCount);
		
		return result;
	}

	private void fileDelete(File deleteFile) {
		Logger.d(TAG, "delete path : " + deleteFile);
		if (deleteFile != null && deleteFile.exists()) {
			deleteFile.delete();
		}
	}

	private void outputDelete(File outputFile) {
		Logger.d(TAG, "outputDelete path : " + outputFile);
		if (outputFile != null && outputFile.exists()) {
			outputFile.delete();
			Toast.makeText(mContext, "외장메모리 이동에 용량이 가득차있습니다." + "삭제 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
		}

	}

	public List<File> getDirFileList(String dirOriginFile) {
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
	
	public static long getDirSize(File originFile) {
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