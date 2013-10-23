package ar.edu.itba.pdc.parser;

import java.util.Map;

public class HttpRequest extends Message {

	private String httpmethod;
	private String URI;
	private String version;
	//hereda el mapa
	//hereda el body ==> pensar para el POST

	//no tiene sentido poder instanciar un request vacio.. pronto a eliminar.
	public HttpRequest() {
	}

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
}
