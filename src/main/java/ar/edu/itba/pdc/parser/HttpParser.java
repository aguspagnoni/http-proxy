package ar.edu.itba.pdc.parser;

import java.nio.ByteBuffer;

import ar.edu.itba.pdc.filters.Filter;
import ar.edu.itba.pdc.filters.TransformationFilter;
import ar.edu.itba.pdc.parser.enumerations.ParsingState;

public class HttpParser {

	public HttpParser() {
	}

	public Message parse(ByteBuffer readBuffer, Message message)
			throws InvalidMessageException {

		if (message == null)
			throw new InvalidMessageException();
		byte[] auxBuf = ByteBuffer.allocate(8192).array();
		readBuffer.flip(); // set the buffer ready to be read

		byte b;
		int i = 0;
		while (true) {

			switch (message.state) {
			case Head:

				// message.increaseHeadersLength(readBuffer.remaining());
				while ((b = readBuffer.get()) != '\n'
						&& readBuffer.hasRemaining())
					auxBuf[i++] = b;

				if (auxBuf[i - 1] == '\r')
					message.firstLine = new String(auxBuf, 0, i - 1);
				else
					message.firstLine = new String(auxBuf);
				if (message.firstLine == null
						|| message.firstLine.length() == 0)
					return null; // empty message
				message.fillHead();
				message.state = ParsingState.Header;

				if (!readBuffer.hasRemaining()) {
					readBuffer.rewind();
					message.saveInBuffer(readBuffer);
					readBuffer.compact();
					return message;
				}
				// break;
			case Header:
				message.increaseHeadersLength(readBuffer.remaining());
				do {
					i = 0;
					while (readBuffer.hasRemaining()) {
						if ((b = readBuffer.get()) != '\n')
							auxBuf[i++] = b;
						else {
							if (i == 0 || i == 1) // case \r\n => 1 and \n => 0
								message.state = ParsingState.Body;
							break;
						}
					}

					if (i > 1) { // last case
						if (auxBuf[i - 1] == '\r')
							message.addHeader(new String(auxBuf, 0, i - 1));
						else
							message.addHeader(new String(auxBuf));
					}

				} while (i > 1); // if is 1 it reached \r\n line
				if (message.state != ParsingState.Body) {
					readBuffer.rewind();
					message.saveInBuffer(readBuffer);
					readBuffer.compact();
					return message;
				}
			case Body:
				TransformationFilter tfilter = TransformationFilter
						.getInstance();
				String contentType = message.headers.get("content-type");
				// System.out.println(contentType);
				if (message.getClass().equals(HttpResponse.class)

				&& !message.headers.containsKey("content-encoding")
						&& contentType != null
						&& contentType.equals("text-plain")
						&& tfilter.filter(message)) {
					while (readBuffer.hasRemaining()
							&& (b = readBuffer.get()) != -1
							&& !message.isFinished()) {
						String changedByte = tfilter.changeByte(b);
						readBuffer.put(readBuffer.position() - 1,
								changedByte.getBytes()[0]);
					}
				} else
					while (readBuffer.hasRemaining()
							&& (b = readBuffer.get()) != -1
							&& !message.isFinished())
						;
				readBuffer.rewind(); // set the buffer ready to a write action
				return message;
			}
		}
	}
}