package ar.edu.itba.pdc.parser;

import java.util.HashSet;
import java.util.Map;

import ar.edu.itba.pdc.parser.enumerations.ParsingState;

public class HttpRequest extends Message {

	private HashSet<String> implementedMethods = new HashSet<String>();
	private HashSet<String> headersHTTP = new HashSet<String>();

	private String httpmethod;
	private String URI;
	private String version;
	protected int headersLength = 0;

	public HttpRequest() {
		implementedMethods.add("post");
		implementedMethods.add("get");
		implementedMethods.add("head");

		headersHTTP.add("host");
		headersHTTP.add("location");
		headersHTTP.add("keep-alive");
		headersHTTP.add("cache-control");
		headersHTTP.add("user-agent");
		headersHTTP.add("accept");
	}

	public HttpRequest(String httpmethod, String host, String version) {
		this.httpmethod = httpmethod;
		this.URI = host;
		this.version = version;
	}

	public void fillHead() {
		String[] aux = firstLine.toLowerCase().split(" ");
		if (firstLine != null && aux.length == 3) {
			this.httpmethod = aux[0];
			this.URI = aux[1];
			this.version = aux[2];
		}
	}

	public boolean isFinished() {
		if (headers.get("host") == null)
			return false;
		if (headers.get("content-length") == null)
			return state.equals(ParsingState.Body);
		setContentLength(Integer.valueOf(headers.get("content-length").trim()));
		return getContentLength() == getAmountRead() - getHeadersLength();
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
