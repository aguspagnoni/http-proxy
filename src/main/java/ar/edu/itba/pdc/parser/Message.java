package ar.edu.itba.pdc.parser;

import java.util.HashMap;
import java.util.Map;

public abstract class Message {

	protected Map<String, String> headers = new HashMap<String, String>();
	protected String body;

	public String getBody() {
		return body;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public abstract String getClientaddr();

	public void setBody(String body) {
		this.body = body;
	}

}
