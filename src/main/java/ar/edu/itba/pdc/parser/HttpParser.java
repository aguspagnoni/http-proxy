package ar.edu.itba.pdc.parser;

import java.nio.ByteBuffer;
import java.util.HashSet;

import ar.edu.itba.pdc.exceptions.BadSyntaxException;
import ar.edu.itba.pdc.parser.enumerations.ParsingState;

public class HttpParser {
	private HashSet<String> headersHTTP = new HashSet<String>();
	private HashSet<String> methodsHTTP = new HashSet<String>();
	private HttpRequest message;
	
	protected ParsingState state;
	protected int headersLength;

	public HttpParser() {
		headersLength = 0;
		state = ParsingState.Header;
		// no hace falta definir los headers.. si los metodos. pronto a ser borrado
		headersHTTP.add("host");
		headersHTTP.add("location");
		headersHTTP.add("keep-alive");
		headersHTTP.add("cache-control");
		headersHTTP.add("user-agent");
		headersHTTP.add("accept");
		// hasta aca
		methodsHTTP.add("post");
		methodsHTTP.add("get");
		methodsHTTP.add("head");
	}
	
	public boolean hasFinished() {
		if (message.headers.get("content-length") == null)
			return true; // cable para ver si es un request
		int contentlength = Integer.valueOf(message.headers.get("content-length"));
		return headersLength != 0 && contentlength == message.body.length();
	}
	
	public ParsingState getState() {
		return state;
	}
	
	public int getHeadersLength() {
		return headersLength;
	}

	public Message parseHeaders(ByteBuffer readBuffer)
			throws BadSyntaxException {
		
		message = new HttpRequest();

		String content = new String(readBuffer.array()).substring(0,
				readBuffer.array().length);
		String[] lines = content.split(System.getProperty("line.separator"));
		String[] firstLine = lines[0].split(" ");
		
		headersLength += lines[0].length(); // +2 por el \r\n ?? ver..
		if (methodsHTTP.contains(firstLine[0].toLowerCase())) {	
			message.setHttpmethod(firstLine[0].toLowerCase());
			message.setURI(firstLine[1].toLowerCase());
			message.setVersion(firstLine[2].toLowerCase());
			int i = 1;
			
			while (i < lines.length && state == ParsingState.Header) {
				headersLength += lines[i].length();
				String[] kv = lines[i].trim().toLowerCase().split(":");
				if (kv.length == 1) {
					if (kv[0] != "")
						message.addHeader(kv[0], "");
					else
						state = ParsingState.Body; // llego el enter
				} else if (kv.length > 2) {
					//TODO ver que pasa en caso de key:value:otracosa  ,ignoramos otracosa?excepcion? ==> ver rfc2616
				} else // == 2
					message.addHeader(kv[0], kv[1]);
				i++;
			}
			
			while (i < lines.length && state == ParsingState.Body) {
				message.body += lines[i];
			}
			return message;
		}
		//no es soportado el metodo ==> TODO devolver excepcion (cn codigo de unsupported de http)
		return null;
	}
}
