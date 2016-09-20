package kr.agworks.sedisk.client.network;

import java.io.File;
import java.net.URLConnection;

public class HttpMultipart {
	
	public enum PropertyKey {
		UserAgent("User-Agent"),
		ContentLength("Content-Length"),
		Range("Range"),
		AcceptCharset("Accept-Charset"),
		ContentType("Content-Type"),
		ContentDisposition("Content-Disposition"),
		ContentTransferEncoding("Content-Transfer-Encoding"),
		Cookie("Cookie"),
		
		@SuppressWarnings("notuse")
		SediskAgent("SEDISK-AGENT"),
		@SuppressWarnings("notuse")
		SediskVer("SEDISK-VER");
		
		private final String key;
		
		PropertyKey(String key) {
			this.key = key;
		}
		
		public String key() {
			return key;
		}
	}
	
	protected static final String CarriageReturn = "\r";
	protected static final String LineFeed = "\n";
	protected static final String CRLF = CarriageReturn + LineFeed;
	protected static final String ContentTypeTextPlain = "text/plain";
	protected static final String DefaultEncoding = "UTF-8";
	
	private String name;
	private String contentType;
	private String contentTextCharset;
	private String parameters;
	private File file;
	private boolean fileAttach;

	public HttpMultipart (String name, String parameters) {
		this.name = name;
		this.contentType = ContentTypeTextPlain;
		this.contentTextCharset = DefaultEncoding;
		this.parameters = parameters;
		this.file = null;
		
		this.fileAttach = false;
	}
	
	public HttpMultipart (String name, File file) {
		this.name = name;
		this.contentType = null;
		this.contentTextCharset = DefaultEncoding;
		this.parameters = null;
		this.file = file;
		
		this.fileAttach = (this.file != null && this.file.exists());
	}
	
	public String getName() {
		return name;
	}
	
	public File getFile() {
		return file;
	}

	public String getContentTextCharset() {
		return contentTextCharset;
	}

	public String getParameters() {
		return parameters;
	}

	public String getContentType() {
		return contentType;
	}
	
	public boolean isFileAttach() {
		return fileAttach;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		//	Content-Disposition
		sb.append(String.format("%s: form-data; name=\"%s\"", PropertyKey.ContentDisposition.key(), name));
					
		if (fileAttach) {
			sb.append(String.format("; filename=\"%s\"", file.getName()));
		}
		
		sb.append(CRLF);
		
		//	Content-Type
		sb.append(String.format("%s: ", PropertyKey.ContentType.key()));
		
		if (fileAttach && contentType == null) {
			sb.append(URLConnection.guessContentTypeFromName(file.getName()));
		} else {
			sb.append(contentType);
		}
		
		if (contentTextCharset != null) {
			sb.append(String.format("; charset=%s", contentTextCharset));
		} 
		
		sb.append(CRLF);
		
		//	Content-Transfer-Encoding
		if (fileAttach) {
			sb.append(String.format("%s: binary", PropertyKey.ContentTransferEncoding.key()));
			sb.append(CRLF);
		}
		
		sb.append(CRLF);	
		
		//	parameter
		if (parameters != null) {
			sb.append(parameters);
		}
		
		sb.append(CRLF);
		
		return sb.toString();
	}

}
