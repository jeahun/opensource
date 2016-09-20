package kr.agworks.sedisk.client.exception;

/**
 * 서버 통신 중 각종 네트워크 관련 문제를 처리하기 위한 exception.
 * 네트워크 문제 발생시 해당 exception 발생.
 * @author ym.chae
 */
public class NetworkDisconnectedException extends Exception {

	private static final long serialVersionUID = 4207045365992676439L;

	public NetworkDisconnectedException() {
		super();
	}
	
	public NetworkDisconnectedException(String message) {
		super(message);
	}
	
	public NetworkDisconnectedException(Throwable cause) {
		super(cause);
	}
	
	public NetworkDisconnectedException(String message, Throwable cause) {
		super(message, cause);
	}
}
