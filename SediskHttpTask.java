package kr.agworks.sedisk.client.network;

import org.json.JSONException;

import com.example.webview.SediskApplication;

import kr.agworks.sedisk.client.exception.NetworkDisconnectedException;
import kr.agworks.sedisk.client.json.PurchaseInfoJSON;
import kr.agworks.sedisk.client.json.SecureKeyJSON;
import kr.agworks.sedisk.client.json.ServerTimeJSON;
import kr.agworks.sedisk.client.network.ServerApis.HttpAPI;
import kr.agworks.sedisk.client.parameters.PurchaseInfoParameters;
import kr.agworks.sedisk.client.security.SediskAesCipher;
import kr.agworks.sedisk.common.util.Logger;
import kr.agworks.sedisk.data.type.DownloadType;
import kr.agworks.sedisk.data.type.TransperType;
import kr.agworks.sedisk.logic.CipherLogic;

/**
 * 
 * Sedisk Http API 통신용 클래스
 *
 */
public class SediskHttpTask {

	private static final String TAG = "SediskHttpTask";
	private static final String MethodPost = "POST";
	private static final String MethodGet = "GET";

	private static SediskHttpTask instance = null;

	public static SediskHttpTask getInstance() {
		if (instance == null) {
			instance = new SediskHttpTask();
		}
		return instance;
	}

	private SediskHttpTask() {

	}


	/**
	 * 서버타임을 반환.
	 * 
	 * @return
	 * @throws NetworkDisconnectedException
	 * @throws JSONException
	 */
	public String getServerTime() throws NetworkDisconnectedException, JSONException {
		HttpResponse response = new HttpHelper().sendTask(MethodGet, ServerApis.getHttpApiUrl(HttpAPI.SERVER_TIME),
				null);
		ServerTimeJSON json = new ServerTimeJSON(response.httpResponseBody);
		return json.rs;
	}

	/**
	 * secure key 반환.
	 * 
	 * @return
	 * @throws NetworkDisconnectedException
	 * @throws JSONException
	 */
	public String getEncryptedSecureKey() throws NetworkDisconnectedException, JSONException {
		HttpResponse response = new HttpHelper().sendTask(MethodGet, ServerApis.getHttpApiUrl(HttpAPI.SECURE_KEY), null);
		SecureKeyJSON json = new SecureKeyJSON(response.httpResponseBody);
		return json.rs;
	}
	
	public String getSecretKey() {
		String decryptSecKey = "";
		try {
			decryptSecKey = CipherLogic.getInstance().getSecretKey();
		} catch (NetworkDisconnectedException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return decryptSecKey;
	}
	
	public PurchaseInfoJSON getPurchaseInfo(String decryptSecKey, int purchaseIndex, int fileIndex, DownloadType downloadType, TransperType transperType) throws NetworkDisconnectedException, JSONException {
		PurchaseInfoParameters param = new PurchaseInfoParameters(purchaseIndex, -1, downloadType, transperType);
		HttpResponse response = new HttpHelper().sendTask(MethodGet, ServerApis.getHttpApiUrl(HttpAPI.PURCHASE_INFO), param, 30000);
		String encryptStr = response.httpResponseBody;
		Logger.d(TAG, "encryptStr : " + encryptStr);
		
		String decryptStr = SediskAesCipher.decrypt(encryptStr, SediskApplication.getSecretKey());
		PurchaseInfoJSON json = new PurchaseInfoJSON(decryptStr);		
		Logger.d(TAG, "decrypt Str : " + decryptStr);
		
		return json;
	}
}