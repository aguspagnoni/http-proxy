package ar.edu.itba.pdc.parser;

import java.nio.ByteBuffer;

import ar.edu.itba.pdc.exceptions.BadSyntaxException;
import ar.edu.itba.pdc.parser.enumerations.ParsingState;

public class HttpParser {

	public HttpParser() {
	}

	public Message parse(ByteBuffer readBuffer, Message message)
			throws BadSyntaxException, InvalidMessageException {

		if (message == null)
			throw new InvalidMessageException();
		byte[] auxBuf = ByteBuffer.allocate(8192).array();
		
		byte b;
		int i = 0;
		switch (message.state) {
		case Head:
			readBuffer.flip();
			while ((b = readBuffer.get()) != '\n')
				auxBuf[i++] = b;

			message.firstLine = new String(auxBuf);
			if (message.firstLine == null || message.firstLine.length() == 0)
				return null; // empty message
			message.fillHead();
			message.state = ParsingState.Header;
			readBuffer.compact();
			break;
		case Header:

			readBuffer.flip();
			while ((b = readBuffer.get()) != '\n')
				auxBuf[i++] = b;

			message.firstLine = new String(auxBuf);
			if (message.firstLine == null || message.firstLine.length() == 0)
				return null; // empty message
			message.fillHead();
			message.state = ParsingState.Header;
			readBuffer.compact();

			// while (i < lines.length && message.state !=
			// ParsingState.Body) {
			// String[] kv = lines[i].trim().toLowerCase().split(":");
			// if (kv.length == 1) {
			// if (!kv[0].equals(""))
			// message.addHeader(kv[0], ""); // key without value
			// else if (lines[i].equals("\r") ){
			// message.state = ParsingState.Body; // llego el enter
			// }
			// } else if (kv.length > 2) {
			// // TODO ver que pasa en caso de key:value:otracosa
			// // ,ignoramos otracosa?excepcion? ==> ver rfc2616
			// } else { // == 2
			// String[] value = kv[1].trim().split("\r"); // this is for the
			// 'key : value\r'
			// message.addHeader(kv[0], value[0]);
			// }
			// i++;
			//
			// }
			// if (i >= lines.length)
			// message.state = ParsingState.Complete; // it was a request
			;
			break;
		case Body:

			if (readBuffer.get() == -1 || message.isFinished()) // cierre de conexion es una forma de indicar q el mensaje se termino.
				message.state = ParsingState.Complete;
			else {
				if (proxy.needsTransformation()) // se aplica on the flyy
					
			}
		case Complete:
			return message;
		}
	}
}