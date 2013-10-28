package ar.edu.itba.pdc.parser;

import ar.edu.itba.pdc.parser.enumerations.ParsingState;

public class HttpResponse extends Message {
	
	protected int code; // e.g. 302, 404
	protected String version; // e.g. HTTP/1.1
	protected String verboseCode; // e.g. Moved Temporarily, Bad Request
	//hereda mapa de headers
	//hereda el body
	
	public boolean isFinished() {
		int headersLength = 0;
		for (String s : firstLine)
			headersLength += s.length();
		for (String s : headers.values())
			headersLength += s.length();
		for (String s : headers.keySet())
			headersLength += s.length();
		String contentlength = headers.get("content-length"); 
		if (code >= 200 && code < 300) // ok response
			return contentlength != null && Integer.valueOf(contentlength) == body.length();
		return true; // cable para los casos como un redirect que no tiene body
	}
	
	public void fillHead() {
		if (firstLine != null) {
			this.version = firstLine[0];
			this.code = Integer.valueOf(firstLine[1]);
			this.verboseCode = firstLine[2];
		}
	}

}
