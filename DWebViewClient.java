package kr.agworks.sedisk.web;

import java.net.URISyntaxException;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import kr.agworks.sedisk.common.util.Logger;
import kr.agworks.sedisk.common.util.ProgressDialogHelper;

public class DWebViewClient extends WebViewClient {
	private static final String TAG = "DWebViewClient";

	private WebviewProgressListener prgListener; 

	public DWebViewClient(Context context, WebviewProgressListener prgListener) {
		this.prgListener = prgListener;
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		super.onPageStarted(view, url, favicon);								
		if (prgListener != null) 
			prgListener.onPageStared();
	}

	@Override
	public void onPageFinished(WebView view, String url) { 
		if (ProgressDialogHelper.isShowingProgress())
			ProgressDialogHelper.closeProgressPopup();
		super.onPageFinished(view, url);

		CookieSyncManager.getInstance().sync();

		if (prgListener != null) 
			prgListener.onPageFinished();
	}

	// 호스트 응용 프로그램에게 오류를 보고합니다. 이러한 오류는 복구할 수 없습니다.
	// 웹뷰에는 인터멧 연결되어 있지 않을때 주소가 노출되는 단점이 있다. 이럴경우 url주소를 보안산 노출되면 안되기 때문에 숨길경우
	// 사용하면 유용할듯 하다.

	@Override
	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
		errorProcess(view);
	}
	/**
	 * return false 해당 URL 브라우저에 랜더링 , return true 해당 URL 랜더링 하지 않음 다
	 * application 실행 시(마켓 포함) 랜더링을 하지 않아야함.
	 *
	 * == 분기 == 1. intent scheme 2. market 3. custom scheme 4. http , https URL
	 */
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		Logger.d(TAG, "url : " + url);
		if (url.startsWith("intent")) { 
			return checkAppInstalled(view, url, "intent"); 
		} else if (url != null && url.startsWith("market://")) { 
			try {
				Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
				if (intent != null) {
					view.getContext().startActivity(intent); 
				}
				return true;
			} catch (URISyntaxException e) { 
				e.printStackTrace(); 
			}
		} else if (url.startsWith("ispmobile")) {
			
			Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(url));
			
			
			

			try {
				view.getContext().startActivity(intent);
			} catch (ActivityNotFoundException ex) {
				intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://mobile.vpay.co.kr/jsp/MISP/andown.jsp"));
				view.getContext().startActivity(intent);
			}

			return true;

		} else if (url.startsWith("http://") || url.startsWith("https://")) {
			return super.shouldOverrideUrlLoading(view, url);
		} else {
			return checkAppInstalled(view, url, "customLink");
		}
		return super.shouldOverrideUrlLoading(view, url);
	}

	/*
	 * application 설치 여부 == 분기 == 1. intent scheme 2. custom scheme //스키마 외부에서 자신에 앱을 접금할 수 있도록 하나의 통로를 열어주는 역활.
	 */
	private boolean checkAppInstalled(WebView view, String url, String type) { 
		if (type.equals("intent")) {
			return intentSchemeParser(view, url); 
		} else if (type.equals("customLink")) { 
			return customSchemeParser(view, url);
		}
		return false;
	}

	/*
	 * intent scheme을 통해 appication 설치 여부 판단 각 카드사 Web에서 javascript를 통한 제어도
	 * 가능하므로 무조건 마켓을 호출하게 되면 중복 처리가 되므로 미설치시 마켓이 호출되지 않는 경우에만 마켓 호출하도록 분기처리
	 */
	private boolean intentSchemeParser(WebView view, String url) {
		boolean returnValue = false;
		try {
			Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME); 
			if (view.getContext().getPackageManager().resolveActivity(intent, 0) == null) {
				String pakagename = intent.getPackage();
				if (pakagename != null) {
					if (url.contains("lotteappcard") || url.contains("shinhan-sr-ansimclick") 
							|| url.contains("lottecard") || url.contains("cloudpay") || url.contains("citispayapp")
							|| url.contains("hdcardappcardansimclick")) {
						Uri uri = Uri.parse("market://details?id=" + pakagename);
						intent = new Intent(Intent.ACTION_VIEW, uri); 
						view.getContext().startActivity(intent);
					}
					return true;
				}
			}
			Uri uri = Uri.parse(intent.getDataString()); 
			intent = new Intent(Intent.ACTION_VIEW, uri);
			view.getContext().startActivity(intent);
			return true;
		} catch (URISyntaxException e) { 
			e.printStackTrace();
		}
		return false; 
	}

	/*
	 * custom scheme을 통한 appication 설치 여부 해당 scheme으로는 패키지명을 알 수 없으므로 패키지명을
	 * 하드코딩하여 설치 여부 판단
	 */
	private boolean customSchemeParser(WebView view, String url) { 
		String packageName = null; 
		if (url.startsWith("shinhan-sr-ansimclick://")) { 
			packageName = "com.shcard.smartpay";
		} else if (url.startsWith("mpocket.online.ansimclick://")) { 
			packageName = "kr.co.samsungcard.mpocket";
		} else if (url.startsWith("hdcardappcardansimclick://")) { 
			packageName = "com.hyundaicard.appcard";
		} else if (url.startsWith("droidxantivirusweb:")) { 
			packageName = "net.nshc.droidxantivirus";
		} else if (url.startsWith("vguardstart://") || url.startsWith("vguardend://")) { 
			packageName = "kr.co.shiftworks.vguardweb";
		} else if (url.startsWith("hanaansim")) { 
			packageName = "com.ilk.visa3d";
		} else if (url.startsWith("nhappcardansimclick://")) { 
			packageName = "nh.smart.mobilecard";
		} else if (url.startsWith("ahnlabv3mobileplus")) {
			packageName = "com.ahnlab.v3mobileplus";
		} else {
			return false;
		}

		Intent intent = null;
	
		if (chkAppInstalled(view, packageName)) {
			try {
				intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
				Uri uri = Uri.parse(intent.getDataString());
				intent = new Intent(Intent.ACTION_VIEW, uri); ;
				view.getContext().startActivity(intent);
				
				return true;
			} catch (URISyntaxException e) { 
				e.printStackTrace();
				}
			/*
			 * intent =
			 * view.getContext().getPackageManager().getLaunchIntentForPackage
			 * (packagePath); view.getContext().startActivity(intent);
			 */
		} else {
			Uri uri = Uri.parse("market://details?id=" + packageName); 
			intent = new Intent(Intent.ACTION_VIEW, uri);
			view.getContext().startActivity(intent);
			return true;
		}

		return false;
	}

	/*
	 * 패키지 설치 여부 판단 후 , 결과 리턴
	 */
	private boolean chkAppInstalled(WebView view, String packagePath) {
		boolean appInstalled = false; 
		try {
			view.getContext().getPackageManager().getPackageInfo(packagePath, PackageManager.GET_ACTIVITIES);
			appInstalled = true;
		} catch (PackageManager.NameNotFoundException e) {
			appInstalled = false;
		}
		return appInstalled;
	}

	protected void errorProcess(final WebView view) {
		if (prgListener != null)
			prgListener.onError(view);
	}

	public interface WebviewProgressListener {
		public void onPageStared();

		public void onPageFinished();

		public void onError(WebView view);
	}
}
