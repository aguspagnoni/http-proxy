package ar.edu.itba.pdc.parser;

import java.util.Map;

public class HttpRequest extends Message {

	private String httpmethod;
	private String URI;
	private String version;
	private String clientaddr;

	// hereda el mapa
	// hereda el body ==> pensar para el POST

	public HttpRequest(String httpmethod, String host, String version) {
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

	protected void addHeader(String key, String value) {
		headers.put(key, value);
	}

	@Override
	public String getClientaddr() {
		return clientaddr;
	}

}
