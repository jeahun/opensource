package com.example.webview;

import java.util.ArrayList;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import kr.agworks.sedisk.client.exception.NetworkDisconnectedException;
import kr.agworks.sedisk.client.json.PurchaseInfoJSON;
import kr.agworks.sedisk.client.network.SediskHttpTask;
import kr.agworks.sedisk.client.network.ServerApis;
import kr.agworks.sedisk.common.util.Logger;
import kr.agworks.sedisk.common.util.ToastUtil;
import kr.agworks.sedisk.data.type.DownloadType;
import kr.agworks.sedisk.data.type.TransperType;
import kr.agworks.sedisk.web.DWebInterface;
import kr.agworks.sedisk.web.DWebInterface.OnWebviewListener;
import kr.agworks.sedisk.web.DWebView;
import kr.agworks.sedisk.web.DWebViewClient;
import kr.agworks.sedisk.web.DWebViewClient.WebviewProgressListener;

public class ActivityMain extends Activity {
	
	private final static String TAG = "ActivityMain";
	private DWebView mMainWebview;
	public static String root = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		
		mMainWebview = (DWebView) findViewById(R.id.wv_main);
		mMainWebview.settings();
		mMainWebview.loadUrl(ServerApis.WEBVIEW_MAIN_URL);

		new Thread(new Runnable() {
			@Override
			public void run() {
				SediskApplication.setSecretKey(SediskHttpTask.getInstance().getSecretKey());

				
			}
		}).start();
		
		LinearLayout linearBack = (LinearLayout)findViewById(R.id.linear_prev);
		linearBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mMainWebview.canGoBack()) {
					mMainWebview.goBack();
				} 
			}
		});
		
		LinearLayout linearNext = (LinearLayout)findViewById(R.id.linear_next);
		linearNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mMainWebview.canGoForward()) {
					mMainWebview.goForward();
				}
			}
		});
		
		LinearLayout linearHome = (LinearLayout)findViewById(R.id.linear_home);
		linearHome.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
		
				

			}
		});
		

		final LinearLayout linearClose = (LinearLayout)findViewById(R.id.linear_close);
		linearClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder closeAlert = new Builder(ActivityMain.this);
				closeAlert.setMessage(getString(R.string.exit_app));
				closeAlert.setCancelable(true);
				closeAlert.setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
				
				closeAlert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				});
				
				closeAlert.show();
			}
		});
		
		LinearLayout linearDown = (LinearLayout)findViewById(R.id.linear_down);
		linearDown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), ActivityDownload.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
			}
		});
		
		LinearLayout linearCharge = (LinearLayout)findViewById(R.id.linear_charge);
		linearCharge.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
		
		LinearLayout linearSetting = (LinearLayout)findViewById(R.id.linear_more);
		linearSetting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
		
		DWebViewClient webviewClient = new DWebViewClient(ActivityMain.this, new WebviewProgressListener() {

			@Override
			public void onPageStared() {
				// TODO Auto-generated method stub
			}
			@Override
			public void onPageFinished() {
				// TODO Auto-generated method stub
			}
			@Override
			public void onError(WebView view) {
				// TODO Auto-generated method stub

			}
		});

		mMainWebview.setWebViewClient(webviewClient);

		mMainWebview.addJavascriptInterface(new DWebInterface(ActivityMain.this, new OnWebviewListener() {
			
			@Override
			public void showWebviewPopup(String url) {

			}

			@Override
			public void onDownload(int purchaseIdx, int downloadType, int transperType) {
				try {
					final PurchaseInfoJSON json = SediskHttpTask.getInstance().getPurchaseInfo(null, purchaseIdx,
							transperType, DownloadType.parse(downloadType), TransperType.parse(transperType));
					
					if (json.result) {
						Logger.d(TAG, "json result : " + json.toString());
						final Dialog dialog = new Dialog(ActivityMain.this);
						dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

						Logger.d(TAG, "onDownload. purchaseIidx : " + purchaseIdx + ", downloadType : " + downloadType
								+ ", transperType : " + transperType);

						View view = getLayoutInflater().inflate(R.layout.dialog_purchase_list, null);

						dialog.setContentView(view);
						Logger.d(TAG, "setContentView : " + dialog);
						ListView lvDownload = (ListView) view.findViewById(R.id.lv_purchase);
						final PurchaseListAdapter adapter = new PurchaseListAdapter(ActivityMain.this,
								json.itemFileList);
						Log.d(TAG, "json : " + json.itemFileList);

						lvDownload.setAdapter(adapter);

						Button btnDwonload = (Button) view.findViewById(R.id.btn_download);
						Button btnCancel = (Button) view.findViewById(R.id.btn_close);
						
						final CheckBox cbSelect = (CheckBox) view.findViewById(R.id.cb_select_all);

						cbSelect.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								if (cbSelect.isChecked()) {
									for (Item item : json.itemFileList) {
										item.isChekced = true;
									}
								} else {
									for (Item item : json.itemFileList) {
										item.isChekced = false;
									}
								}

								adapter.notifyDataSetChanged();
							}
						});

						btnCancel.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}
						});

						btnDwonload.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								final ArrayList<Item> fileList = new ArrayList<Item>();
								
								for (Item item : json.itemFileList) {
									if (item.isChekced) {
										fileList.add(item);
									}
								}

								if (fileList.size() <= 0) {
									ToastUtil.showMakeText(ActivityMain.this, getString(R.string.select_download_file),
											Toast.LENGTH_LONG);
								} else {
									//다운로드 시작
									startDownload(fileList);
									
								}

							}
							
						});
						android.view.WindowManager.LayoutParams param = dialog.getWindow().getAttributes();
						param.width = android.widget.LinearLayout.LayoutParams.MATCH_PARENT;
						dialog.getWindow().setAttributes(param);
						dialog.show();
					} else {

					}
				} catch (NetworkDisconnectedException | JSONException e) {
					Logger.e(TAG, "err. ", e);
				}
			}
		}), "AndroidBridge");
	}
	
	

	
	private void startDownload(ArrayList<Item> fileList) {
		
		Intent intent = new Intent(ActivityMain.this, ActivityDownload.class);
		intent.putParcelableArrayListExtra("List", fileList);
		Log.d(TAG, "List : " + fileList);
		startActivity(intent);
	}
	private void moveHome() {
		mMainWebview.loadUrl(ServerApis.WEBVIEW_MAIN_URL);
	}
	
	
}
