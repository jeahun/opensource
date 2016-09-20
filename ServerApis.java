package kr.agworks.sedisk.client.network;


/**
 * 서버 API 호출시 사용되는 클래스
 * @author ymchae
 *
 */
public class ServerApis {
	public static final String PROTOCOL = "http://";
	public static final String HOST = "m.sedisk.com";
	public static final String TEST_HOST = "m0.sedisk.com";
	
	public static final String API_VER = "v1.1";
	
	public static final String API_COMMON_PATH = "/app/" + "defAPI.php";
	public static final String API_VER_PATH = "/app/" + API_VER + "/appAPI.php";
	
	public static final String LOGOUT_PATH = "/doc.php?doc=logout";
	
	public static final String PATH_GCM_REGISTER = "http://dist.sedisk.com/gcmMemberUpdate.php";
	
	public static final String WEBVIEW_MAIN_URL = PROTOCOL + HOST;
	public static final String WEBVIEW_TEST_MAIN_URL = PROTOCOL + TEST_HOST;
	
	public static final String WEBVIEW_LOGOUT_URL = PROTOCOL + HOST + LOGOUT_PATH;
	
//	public static final String WEBVIEW_CHARGE_URL = "http://m.sedisk.com/charge2.php";
	public static final String WEBVIEW_CHARGE_URL = "javascript:goCharge('point')";
	
	public static final String SPEED_CHECK_URL = "http://157.7.94.113/api/spdChk.php?status=";
	
	/**
	 * 
	 * Http로 통신하는 API들
	 *
	 */
	public enum HttpAPI {
		/** 구매 정보 반환*/
		PURCHASE_INFO,
		/**서버 타임 반환 */
		SERVER_TIME,
		/**암호화 키 반환 */
		SECURE_KEY;
		
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder("act=");
			
			switch(this) {
			case PURCHASE_INFO : 
				result.append("purchaseinfo"); 
				break;
			case SERVER_TIME: 
				result.append("ymd"); 
				break;
			case SECURE_KEY: 
				result.append("gk"); 
				break;
			default:
				break;
			}
			
			return result.toString();
		}
		
	}
	
	private static String getVerApiUrl(HttpAPI query) {
		String url = new StringBuilder(PROTOCOL).append(HOST).append(API_VER_PATH).append("?").append(query.toString()).toString();
		return url;
	}
	
	/**
	 * 
	 * Http 통신 API URL 반환 
	 * 
	 * @param query : HttpAPI 클래스 참조
	 * @return
	 */
	public static String getHttpApiUrl(HttpAPI query) {
		String url;
		
		switch (query) {
		case PURCHASE_INFO:
		case SECURE_KEY:
		case SERVER_TIME:
		default:
			//ver API
			url = getVerApiUrl(query);
			break;
		}
		
		return url;
	}
	
}
