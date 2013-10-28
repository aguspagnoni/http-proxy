package ar.edu.itba.pdc.parser;

import java.nio.ByteBuffer;

import ar.edu.itba.pdc.exceptions.BadSyntaxException;
import ar.edu.itba.pdc.parser.enumerations.ParsingState;

public class HttpParser {

	public HttpParser() {
	}

	public Message parse(ByteBuffer readBuffer, Message message, long bytesRead)
			throws BadSyntaxException, InvalidMessageException {
		
		if (message == null)
			throw new InvalidMessageException();

		String content = new String(readBuffer.array()).substring(0, (int) bytesRead);
		int i = 1;
//		String[] lines = content.split(System.getProperty("line.separator"));
		String[] lines = content.split("\n");
		while (true) {
			switch (message.state) {
			case Head:
				if (lines[0] != null)
				message.firstLine = lines[0].toLowerCase().split(" ");
				if (message.firstLine == null || message.firstLine.length == 0)
					return null; // empty message
				message.fillHead();
				message.state = ParsingState.Header;
				break;
			case Header:

				while (i < lines.length && message.state != ParsingState.Body) {
					String[] kv = lines[i].trim().toLowerCase().split(":");
					if (kv.length == 1) {
						if (!kv[0].equals(""))
							message.addHeader(kv[0], ""); // key without value
						else if (lines[i].equals("\r") ){
							message.state = ParsingState.Body; // llego el enter
						}
					} else if (kv.length > 2) {
						// TODO ver que pasa en caso de key:value:otracosa
						// ,ignoramos otracosa?excepcion? ==> ver rfc2616
					} else { // == 2
						String[] value = kv[1].trim().split("\r"); // this is for the 'key : value\r'
						message.addHeader(kv[0], value[0]); 
					}
					i++;

				}
//				if (i >= lines.length)
//					message.state = ParsingState.Complete; // it was a request
				;
				break;
			case Body:
				while (i < lines.length && message.state == ParsingState.Body) {
					
					int contentlength = Integer.valueOf(message.headers.get("content-length"));
//					if (i == lines.length - 1)
//						 lines[i] = lines[i].trim();
					if (message.body.length() + lines[i].length() > contentlength) {
						int j = 0;
						for (; message.body.length() + j < contentlength; j++)
							message.body += lines[i].charAt(j);
					} else
						message.body += lines[i]; // ver que pasa si mandan un \n..
												// pq lo estaria sacando cuando
												// hace el split por \n
					if (message.isFinished())
						message.state = ParsingState.Complete;
					i++;
				}
				return message;
			case Complete:
				return message;
			}
		}
	}
}
