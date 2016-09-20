package com.example.webview;


import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import kr.agworks.sedisk.common.util.Logger;
import kr.agworks.sedisk.data.type.ApplicationType;

public class SediskApplication extends Application {
	private static final String TAG = "SediskApplication";
	
	private static ApplicationType mAppType;
	private static String mAppver; 
	
	private static String mSecretKey;
	
	@Override
	public void onCreate() {
		super.onCreate();
		//로그 활성화 세팅
		Logger.setAllLogEnable(getApplicationContext(), getResources().getBoolean(R.bool.log_enable));
		Logger.d(TAG, "onCreate()");
		
		init();
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		Logger.d(TAG, "onTerminate()");
	}
	
	private void init() {
		mAppType = ApplicationType.parse(getResources().getString(R.string.app_type));
		mAppver = getAppVersion(getApplicationContext());
	}
	

	public static ApplicationType getmAppType() {
		return mAppType;
	}
	
	public static String getAppVer() {
		return mAppver;
	}
	
	public static void setSecretKey(String key) {
		mSecretKey = key;
	}
	
	public static String getSecretKey() {
		return mSecretKey;
	}
	
	
	public static String getAppVersion(Context context) {
		String version = "";
		try {
			PackageInfo i = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			version = i.versionName;
		} catch (Exception e) {
			Logger.e(TAG, "getAppVersion failed. ", e);
		}
		return version;
	}
	
}
