package ar.edu.itba.pdc.parser;

import java.nio.ByteBuffer;
import java.util.HashSet;

import ar.edu.itba.pdc.exceptions.BadSyntaxException;

public class HttpParser {
	private HashSet<String> headersHTTP = new HashSet<String>();
	private HashSet<String> methodsHTTP = new HashSet<String>();

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

	public HTTPRequest parseHeaders(ByteBuffer readBuffer)
			throws BadSyntaxException {
		
		HTTPRequest httpHeaders = new HTTPRequest();

		//TODO podria no ser todo el contenido, habria que ver que pasa cuando viene segmentado el request ==> estados
		String fullContent = new String(readBuffer.array()).substring(0,
				readBuffer.array().length);
		String[] lines = fullContent.split(System.getProperty("line.separator"));
		String[] firstLine = lines[0].split(" ");
		
		if (methodsHTTP.contains(firstLine[0].toLowerCase())) {	
			httpHeaders.setHttpmethod(firstLine[0].toLowerCase());
			httpHeaders.setURI(firstLine[1].toLowerCase());
			httpHeaders.setVersion(firstLine[2].toLowerCase());
			
			for (int i = 1; i < lines.length; i++) {
			
				String[] kv = lines[i].trim().toLowerCase().split(":");
//				if (headersHTTP.contains(kv[0])) {
					if (kv.length == 1) {
						httpHeaders.addHeader(kv[0], "");
					} else if (kv.length > 2) {
						//TODO ver que pasa en caso de key:value:otracosa  ,ignoramos otracosa?excepcion? ==> ver rfc2616
					} else // == 2
						httpHeaders.addHeader(kv[0], kv[1]);
//				}
			}
			return httpHeaders;
		}
		//no es soportado el metodo ==> TODO devolver excepcion (cn codigo de unsupported de http)
		return null;
	}
}
