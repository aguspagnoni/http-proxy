package ar.edu.itba.pdc.parser;

import java.util.HashMap;
import java.util.Map;

public class HTTPHeaders {

	private String httpmethod;
	private String URI;
	private String version;
	private Map<String, String> headers = new HashMap<String, String>();

	public HTTPHeaders() {
	}

	public HTTPHeaders(String httpmethod, String host, String version) {
		this.httpmethod = httpmethod;
		this.URI = host;
		this.version = version;
	}

	public String getHttpmethod() {
		return httpmethod;
	}

	public String getURI() {
		return URI;
	}

	public void setURI(String host) {
		this.URI = host;
	}

	public String getVersion() {
		return version;
	}

	public void setHttpmethod(String httpmethod) {
		this.httpmethod = httpmethod;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}


}
