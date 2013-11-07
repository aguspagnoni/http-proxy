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
		readBuffer.flip();
		
		byte b;
		int i = 0;
		while (true) {
			
			switch (message.state) {
			case Head:
				while ((b = readBuffer.get()) != '\n' && readBuffer.hasRemaining())
					auxBuf[i++] = b;
	
				message.firstLine = new String(auxBuf);
				if (message.firstLine == null || message.firstLine.length() == 0)
					return null; // empty message
				message.fillHead();
				message.state = ParsingState.Header;
//				readBuffer.compact();
				break;
			case Header:
	
//				readBuffer.flip();
				do {
					i = 0;
					while ((b = readBuffer.get()) != '\n' && readBuffer.hasRemaining())
						auxBuf[i++] = b;
					
					if (i > 1) // last case
						message.addHeader(new String(auxBuf, 0, i-1));
					
				} while (i > 1); // if is 1 it reached \r\n line
				
				message.state = ParsingState.Body;
//				readBuffer.compact();
				break;
			case Body:
					// filter.transform()
	//			boolean transformationOn = filter.applyTransformation && message.headers.get("content-type").contains("text/plain");
	//			if (transformationOn) {
	//				ByteBuffer transformedBuffer = ByteBuffer.allocate(readBuffer.capacity());
					while (readBuffer.hasRemaining() && (b = readBuffer.get()) != -1 && !message.isFinished()); // cierre de conexion (-1) es una forma de indicar q el mensaje se termino.
//						readBuffer.put(readBuffer.arrayOffset() + i, b);
	//			}
				
				readBuffer.rewind();
				return message; // hasta q no este lo de la transformacion se mmanda asi como viene
			case Complete:
				readBuffer.rewind();
				return message;
			}
		}
	}
}