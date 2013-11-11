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
		readBuffer.flip(); // set the buffer ready to be read
		
		byte b;
		int i = 0;
		while (true) {
			
			switch (message.state) {
			case Head:
				while ((b = readBuffer.get()) != '\n' && readBuffer.hasRemaining())
					auxBuf[i++] = b;
	
				if (auxBuf[i-1] == '\r')
					message.firstLine = new String(auxBuf, 0, i-1);
				else
					message.firstLine = new String(auxBuf);
				if (message.firstLine == null || message.firstLine.length() == 0)
					return null; // empty message
				message.fillHead();
				message.state = ParsingState.Header;
				break;
			case Header:
				
				message.increaseHeadersLength(readBuffer.remaining()); //  check if this is done correctly
				do {
					i = 0;
					while (readBuffer.hasRemaining() && (b = readBuffer.get()) != '\n')
						auxBuf[i++] = b;
					
					if (i > 1) { // last case
						if (auxBuf[i-1] == '\r')
							message.addHeader(new String(auxBuf, 0, i-1));
						else
							message.addHeader(new String(auxBuf));
					}
					
				} while (i > 1); // if is 1 it reached \r\n line
				message.state = ParsingState.Body;
				break;
			case Body:
					// filter.transform()
	//			boolean transformationOn = filter.applyTransformation && message.headers.get("content-type").contains("text/plain");
	//			if (transformationOn) {
	//				ByteBuffer transformedBuffer = ByteBuffer.allocate(readBuffer.capacity());
					while (readBuffer.hasRemaining() && (b = readBuffer.get()) != -1 && !message.isFinished()); // cierre de conexion (-1) es una forma de indicar q el mensaje se termino.
//						readBuffer.put(readBuffer.arrayOffset() + i, b);
	//			}
				
				readBuffer.rewind(); // set the buffer ready to a write action
				return message; // hasta q no este lo de la transformacion se mmanda asi como viene
			case Complete:
				readBuffer.rewind(); // set the buffer ready to a write action
				return message;
			}
		}
	}
}