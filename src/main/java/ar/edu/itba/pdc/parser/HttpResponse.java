package ar.edu.itba.pdc.parser;

import java.util.Map;

public class HttpResponse extends Message {

	private int statuscode;
	private String clientaddr;

	// hereda mapa de headers
	// hereda el body

	public int getStatusCode() {
		return statuscode;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public String getClientaddr() {
		return clientaddr;
	}

	public void setClientaddr(String clientaddr) {
		this.clientaddr = clientaddr;
	}

	public void setBody(String body) {
		this.body = body;
	}
}
