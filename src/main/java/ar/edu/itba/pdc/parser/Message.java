package ar.edu.itba.pdc.parser;

import java.util.HashMap;
import java.util.Map;

import ar.edu.itba.pdc.parser.enumerations.ParsingState;

public abstract class Message {
	
	protected Map<String, String> headers = new HashMap<String, String>();
	protected String body = "";
	protected String[] firstLine;
	protected ParsingState state = ParsingState.Head;
	
	protected void addHeader(String key, String value) {
		headers.put(key, value);
	}
	
	public abstract boolean isFinished();
	
	/* 
	 * Implementations should make the first line of a message useful. 
	 * 
	 * e.g. HTTP request's head:	GET /resource HTTP/1.1
	 * 
	 * This method should recognize the method, resource path and the protocol version.
	 * */
	public abstract void fillHead();

}
