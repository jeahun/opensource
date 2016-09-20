package kr.agworks.sedisk.web;

import android.content.Context;
import android.webkit.JavascriptInterface;
import kr.agworks.sedisk.common.util.Logger;

public class DWebInterface {
	private static final String TAG = "DWebInterface";
	private Context mContext;
	private OnWebviewListener listener;
	
//	자바스크립트 동장에 반응 호출 연결부 클래스파일
	public DWebInterface(Context context) {
		this.mContext = context;
	}
	
	public DWebInterface(Context context, OnWebviewListener listener) {
		this.mContext = context;
		this.listener = listener;
	}
	
	@JavascriptInterface
	public void contentsdownload(int purchaseIidx, int downloadType, int transperType) {
		Logger.d(TAG, "contentsdownload. purchaseIidx : " + purchaseIidx + ", downloadType : " + downloadType + ", transperType : " + transperType);
		
		if (listener != null)
			listener.onDownload(purchaseIidx, downloadType, transperType);
	}
	
	@JavascriptInterface
	public void requestShowWebDialog(String url) {
		if (listener != null)
			listener.showWebviewPopup(url);
	}
		
	public interface OnWebviewListener {
		void onDownload(int purchaseIdx, int downloadType, int transperType);
		void showWebviewPopup(String url);
	}
}
