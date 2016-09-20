package kr.agworks.sedisk.web;

import com.example.webview.SediskApplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;
import kr.agworks.sedisk.client.network.ConnectionConst;
import kr.agworks.sedisk.common.util.Logger;

public class DWebView extends WebView {
	private static final String TAG = "DWebView";
	
	public DWebView(Context context) {
		super(context);
		settings();
	}
	
	public DWebView(Context context, AttributeSet attributeSet) {
	    super(context, attributeSet);
	}
	

	@SuppressLint({ "NewApi", "SetJavaScriptEnabled" })
	public void settings() {
		Logger.d(TAG, "settings()");
		WebSettings settings = getSettings();
		
		String orginAgent = settings.getUserAgentString();
		StringBuffer sb = new StringBuffer(orginAgent);
		sb.append(ConnectionConst.AGENT_CHECK_NAME);
		sb.append(SediskApplication.getmAppType().getType());
		sb.append(ConnectionConst.AGENT_CHECK_SEPARATOR);
		sb.append(SediskApplication.getAppVer());
		
		settings.setUserAgentString(sb.toString());
		
		Logger.d(TAG, "agent : " + settings.getUserAgentString());
		
		
		settings.setJavaScriptEnabled(true); //인터페이스 등록
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		
		settings.setAppCacheEnabled(false);
		settings.setPluginState(WebSettings.PluginState.ON);

		try {
			settings.setMediaPlaybackRequiresUserGesture(false);
		} catch(NoSuchMethodError e) {
			
		}
		
		settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
		settings.setLoadWithOverviewMode(true);
		settings.setSupportMultipleWindows(true);
		
		settings.setDomStorageEnabled(true);
		settings.setNeedInitialFocus(false);
		
		setHorizontalScrollBarEnabled(false);
		setVerticalScrollBarEnabled(false);
	}
	
}