package ar.edu.itba.pdc.parser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import ar.edu.itba.pdc.exceptions.BadSyntaxException;

public class HttpParser {
	private List<String> headersHTTP = new ArrayList<String>();
	private List<String> methodsHTTP = new ArrayList<String>();

	private static HttpParser instance;

	private HttpParser() {
		headersHTTP.add("host");
		headersHTTP.add("location");
		headersHTTP.add("keep-alive");
		headersHTTP.add("cache-control");
		headersHTTP.add("user-agent");
		headersHTTP.add("accept");
		methodsHTTP.add("post");
		methodsHTTP.add("get");
		methodsHTTP.add("head");
	}

	public static HttpParser getInstance() {
		if (instance == null)
			instance = new HttpParser();
		return instance;
	}

	public HTTPHeaders parseHeaders(ByteBuffer readBuffer)
			throws BadSyntaxException {
		int i = 0, j = 0;
		HTTPHeaders httpHeaders = new HTTPHeaders();

		String fullLine = new String(readBuffer.array()).substring(0,
				readBuffer.array().length);
		for (String header : fullLine.split(System.getProperty("line.separator"))) {
			if (i == 0) {
				for (String firstLine : header.split(" ")) {
					if (j == 0) {
						methodsHTTP.contains(firstLine.toLowerCase());
						httpHeaders.setHttpmethod(firstLine.toLowerCase());
					} else if (j == 1) {
						httpHeaders.setURI(firstLine.toLowerCase());
					} else if (j == 2) {
						httpHeaders.setVersion(firstLine.toLowerCase());

					
					}
					j++;
				}

			} else {
				String[] aux = header.toLowerCase().split(":");
				String trimmed = aux[0].trim();
				if (headersHTTP.contains(trimmed)) {

					if (aux.length > 1) {
						httpHeaders.getHeaders().put(trimmed, aux[1].trim());
					} else {
						httpHeaders.getHeaders().put(trimmed, "");
					}
				}

			}
			i++;
		}
		return httpHeaders;
	}
}
