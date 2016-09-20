package kr.agworks.sedisk.client.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;

import com.example.webview.SediskApplication;

import android.accounts.NetworkErrorException;
import android.text.TextUtils;
import android.webkit.CookieManager;
import kr.agworks.sedisk.client.exception.NetworkDisconnectedException;
import kr.agworks.sedisk.client.network.HttpMultipart.PropertyKey;
import kr.agworks.sedisk.client.parameters.HttpParameterObject;
import kr.agworks.sedisk.common.util.Logger;

public class HttpHelper {
	private final static String TAG = "HttpHelper";
	
	private static final String MethodPost = "POST";
	private static final String MethodGet = "GET";
	
	private final int connectionTimeout = 20000; //20초
	
	public HttpResponse sendMultipartTask(String method, String hostUrl, HttpMultipart[] multiparts) throws Exception  {
		HttpTaskWorker httpTaskThread = new HttpTaskWorker(hostUrl, multiparts);
		
		try {
			httpTaskThread.start();
			httpTaskThread.join();
		} catch (Exception e) {
			Logger.e(TAG, "sendMultipartTask error.", e);
			throw new NetworkDisconnectedException("server is disconnected", e);
		}
		
		if (httpTaskThread.exception != null ) {
			Logger.e(TAG, "api failed ", httpTaskThread.exception);
			throw new NetworkDisconnectedException("api failed", httpTaskThread.exception);
		}
		
		return httpTaskThread.response;
	}
	
	public HttpResponse sendTask(String method, String hostUrl, HttpParameterObject param) throws NetworkDisconnectedException  {
		return sendTask(method, hostUrl, param, connectionTimeout);
	}
	
	public HttpResponse sendTask(String method, String hostUrl, HttpParameterObject param, int timeout) throws NetworkDisconnectedException  {
		HttpTaskWorker httpTaskThread = new HttpTaskWorker(method, hostUrl, param);
		
		try {
			httpTaskThread.start();
			httpTaskThread.join(timeout);
		} catch (Exception e) {
			Logger.e(TAG, "sendTask error.", e);
			throw new NetworkDisconnectedException("server is disconnected",  e);
		}
		
		if (httpTaskThread.exception != null ) {
			Logger.e(TAG, "api failed ", httpTaskThread.exception);
			throw new NetworkDisconnectedException("api failed", httpTaskThread.exception);
		}
		
		//	서버와 응답이 실패했을경우
		if (httpTaskThread.response == null) {
			Logger.e(TAG, "response from %s : %s", method, "null");
			throw new NetworkDisconnectedException("server is disconnected.");
		}
		
		return httpTaskThread.response;
	}
	
	
	public HttpResponse sendMultipart(String hostUrl, HttpMultipart[] multiparts) throws Exception {
		HttpResponse response = new HttpResponse();
		HttpURLConnection conn = null;
		BufferedReader reader = null;
		StringBuffer sb = new StringBuffer();
		PrintWriter writer = null;
		OutputStream os = null;
		
		try {
			URL url = new URL(hostUrl);
			
			conn = (HttpURLConnection) url.openConnection();
			
			conn.setConnectTimeout(connectionTimeout);
			conn.setRequestMethod(MethodPost);
			
			String userAgent = System.getProperty(ConnectionConst.AGENT_PROPERTY_NAME);
			StringBuffer sbf = new StringBuffer(userAgent);
			sbf.append(ConnectionConst.AGENT_CHECK_NAME);
			sbf.append(SediskApplication.getmAppType().getType());
			sbf.append(ConnectionConst.AGENT_CHECK_SEPARATOR);
			sbf.append(SediskApplication.getAppVer());
			
			conn.setRequestProperty(PropertyKey.UserAgent.key(), sbf.toString());
			
			String boundary = Long.toHexString(System.currentTimeMillis());
			conn.setDoInput(true);
	        conn.setDoOutput(true);
	        conn.setUseCaches(false);
			conn.setRequestProperty(PropertyKey.ContentType.key(), "multipart/form-data;boundary=" + boundary);
			
			os = conn.getOutputStream();
			
			writer = new PrintWriter(new OutputStreamWriter(os, HttpMultipart.DefaultEncoding), true);
			
			for (HttpMultipart multipart : multiparts) {
				
				if (!multipart.isFileAttach()) {
					
					writer.append("--" + boundary).append(HttpMultipart.CRLF);
			        writer.append("Content-Disposition: form-data; name=\"" + multipart.getName() + "\"")
			                .append(HttpMultipart.CRLF);
			        writer.append("Content-Type: text/plain; charset=" + HttpMultipart.DefaultEncoding).append(
			        		HttpMultipart.CRLF);
			        writer.append(HttpMultipart.CRLF);
			        writer.append(multipart.getParameters()).append(HttpMultipart.CRLF);
			        writer.flush();
				} else {
					
					String fileName = multipart.getFile().getName();
			        writer.append("--" + boundary).append(HttpMultipart.CRLF);
			        writer.append(
			                "Content-Disposition: form-data; name=\"" + multipart.getName()
			                        + "\"; filename=\"" + fileName + "\"")
			                .append(HttpMultipart.CRLF);
			        writer.append(
			                "Content-Type: "
			                        + URLConnection.guessContentTypeFromName(fileName))
			                .append(HttpMultipart.CRLF);
			        writer.append("Content-Transfer-Encoding: binary").append(HttpMultipart.CRLF);
			        writer.append(HttpMultipart.CRLF);
			        writer.flush();
			 
			        FileInputStream inputStream = new FileInputStream(multipart.getFile());
			        byte[] buffer = new byte[4096];
			        int bytesRead = -1;
			        while ((bytesRead = inputStream.read(buffer)) != -1) {
			            os.write(buffer, 0, bytesRead);
			        }
			        os.flush();
			        inputStream.close();
			         
			        writer.append(HttpMultipart.CRLF);
			        writer.flush();    
				}
			}
			
			writer.append("--" + boundary + "--");
			writer.append(HttpMultipart.CRLF);
			writer.flush();
				
			response.httpResponseCode = conn.getResponseCode();
			response.httpResponseMessage = conn.getResponseMessage();
			
			try {
				if (response.httpResponseCode >= 200 && response.httpResponseCode < 300) {
					reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				} else {
					reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
				}

				String line = null;
				
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
			} catch (IOException e) {
				if (response.httpResponseCode >= 200 && response.httpResponseCode < 300) {
					throw e;
				}
			}
			
			response.httpResponseBody = sb.toString();
			
		} catch (Exception e) {
			throw new NetworkErrorException();
		} finally {
			if (writer != null) {
				try {
					writer.close();
					writer = null;
				} catch (Exception e2) {}
			}
			if (reader != null) {
				try {
					reader.close();
					reader = null;
				} catch (Exception e2) {}
			}
			if (writer != null) {
				try {
					writer.close();
					writer = null;
				} catch (Exception e2) {}
			}
			if (conn != null) {
				try {
					conn.disconnect();
					conn = null;
				} catch (Exception e2) {}
			}
		}
		
		return response;
	}
	
	public HttpResponse send(String method, String hostUrl, HttpParameterObject param) throws Exception {
		HttpURLConnection conn = null;
		HttpResponse response = new HttpResponse();
		BufferedReader reader = null;
		StringBuffer sb = new StringBuffer();
		OutputStreamWriter writer = null;
		URL url = null;
		
		try {
			if (method.equals(MethodGet) && param != null) {
				url = new URL(hostUrl + param.toHttpParameterString());
			} else {
				url = new URL(hostUrl);
			}
			
			Logger.v(TAG, "url : " + url);
			
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(connectionTimeout);
			conn.setRequestMethod(method);
			
			conn.setRequestProperty(PropertyKey.AcceptCharset.key(), HttpMultipart.DefaultEncoding);
			
			//default
			String userAgent = System.getProperty(ConnectionConst.AGENT_PROPERTY_NAME);
			StringBuffer sbf = new StringBuffer(userAgent);
			sbf.append(ConnectionConst.AGENT_CHECK_NAME);
			sbf.append(SediskApplication.getmAppType().getType());
			sbf.append(ConnectionConst.AGENT_CHECK_SEPARATOR);
			sbf.append(SediskApplication.getAppVer());
			conn.setRequestProperty(PropertyKey.UserAgent.key(), sbf.toString());
			String cookie = CookieManager.getInstance().getCookie(ServerApis.WEBVIEW_MAIN_URL);
			conn.setRequestProperty(PropertyKey.Cookie.key(), cookie);
			
			String paramStr = null;
			
			if (method.equals(MethodPost)) {
				conn.setDoOutput(true);
				
				if (param != null) {
					paramStr = param.toHttpParameterString();
				}
			}
			
			if (!TextUtils.isEmpty(paramStr)) {
				writer = new OutputStreamWriter(conn.getOutputStream());
				writer.write(paramStr);
				writer.flush();
			}
			
			conn.connect();
						
			response.httpResponseCode = conn.getResponseCode();
			response.httpResponseMessage = conn.getResponseMessage();
			
			try {
				if (response.httpResponseCode >= 200 && response.httpResponseCode < 300) {
					reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), HttpMultipart.DefaultEncoding));
				} else {
					reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
				}

				String line = null;
				
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
			} catch (IOException e) {
				if (response.httpResponseCode >= 200 && response.httpResponseCode < 300) { 
					throw e;
				}
			}
			
			response.httpResponseBody = sb.toString();
			
		} catch (Exception e) {
			throw e;
		} finally {
			if (reader != null) {
				try {
					reader.close();
					reader = null;
				} catch (Exception e2) {}
			}
			
			if (conn != null) {
				try {
					conn.disconnect();
					conn = null;
				} catch (Exception e2) {}
			}
			
			if (writer != null) {
				try {
					writer.close();
					writer = null;
				} catch (Exception e2) {}
			}
		}
		
		return response;
	}
	
	
	public static String post(String url, HashMap<String, String> data) {
		
		String response = null;
		
		StringBuilder builder = new StringBuilder();
		Iterator<String> iterID = data.keySet().iterator();
		while(iterID.hasNext()) {
			String id = iterID.next();
			String value = data.get(id);
			
			builder.append(id).append("=").append(value);
			if(iterID.hasNext()) {
				builder.append("&");
			}
		}
		
		String strData = builder.toString();
		
		HttpURLConnection conn = null;
		OutputStream out = null;
		InputStream input = null;
		
		try {
			URL conUrl = new URL(url);
			conn = (HttpURLConnection)conUrl.openConnection();
			conn.setRequestMethod(MethodPost);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			
			String userAgent = System.getProperty(ConnectionConst.AGENT_PROPERTY_NAME);
			StringBuffer sbf = new StringBuffer(userAgent);
			sbf.append(ConnectionConst.AGENT_CHECK_NAME);
			sbf.append(SediskApplication.getmAppType().getType());
			sbf.append(ConnectionConst.AGENT_CHECK_SEPARATOR);
			sbf.append(SediskApplication.getAppVer());
			
			conn.setRequestProperty(PropertyKey.UserAgent.key(), sbf.toString());
			
			conn.connect();
			
			out = conn.getOutputStream();
			out.write(strData.toString().getBytes());
			out.flush();
			out.close();
			
			if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				input = new BufferedInputStream(conn.getInputStream()); 
	            StringBuffer result = new StringBuffer(); 
				
	            byte[] buffer = new byte[1024];
	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            
	            int nLength = 0;
				while( (nLength = input.read(buffer, 0, buffer.length)) != -1) {
					baos.write(buffer, 0, nLength);
				}
				
				response = new String(baos.toByteArray());
			}
            
		} catch(Exception e) {
			Logger.e(TAG, "error.", e);
		} finally {
			try {
				if(out != null) {
					out.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Logger.e(TAG, "error.", e);
			}
			
			try {
				if(input != null) {
					input.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Logger.e(TAG, "error.", e);
			}
			
			conn.disconnect();
		}
		
		return response;
	}
	
	private class HttpTaskWorker extends Thread {
		private String methodName = null;
		private boolean result = false;
		private Exception exception = null;
		private HttpResponse response = null;
		private HttpParameterObject param = null;
		private HttpMultipart[] multiparts = null;
		private boolean isSendMultipart = false;
		
		private String apiUrl = null;
		
		public HttpTaskWorker(String method, String hostUrl, HttpParameterObject param) {
			this.methodName = method;
			this.apiUrl = hostUrl;
			this.param = param;
			this.isSendMultipart = false;
		}
		
		public HttpTaskWorker(String hostUrl, HttpMultipart[] multiparts) {
			this.apiUrl = hostUrl;
			this.multiparts = multiparts;
			this.isSendMultipart = true;
		}

		@Override
		public void run() {
			try {
				if (isSendMultipart) {
					response = sendMultipart(apiUrl, multiparts);
				} else {
					response = send(methodName, apiUrl, param);
				}
				result = true;
				exception = null;
			} catch (Exception e) {
				exception = e;
			}
		}
	}
}
