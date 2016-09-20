package kr.agworks.sedisk.client.network;

public class HttpResponse {
	public int httpResponseCode;
	public String httpResponseMessage;
	public String httpResponseBody;
	
	@Override
	public String toString() {
		return String.format("[httpResponseCode:%s, httpResponseMessage:%s, httpResponseBody:%s]", httpResponseCode, httpResponseMessage, httpResponseBody);
	}
}
