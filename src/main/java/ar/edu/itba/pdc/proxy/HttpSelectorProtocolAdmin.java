package ar.edu.itba.pdc.proxy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import ar.edu.itba.pdc.exceptions.BadSyntaxException;
import ar.edu.itba.pdc.logger.HTTPProxyLogger;
import ar.edu.itba.pdc.parser.HttpParser;
import ar.edu.itba.pdc.parser.PDCRequest;
import ar.edu.itba.pdc.parser.PDCResponse;
import ar.edu.itba.pdc.parser.enumerations.ParsingState;

public class HttpSelectorProtocolAdmin implements TCPProtocol {

	private Map<SocketChannel, ChannelBuffers> list = new HashMap<SocketChannel, ChannelBuffers>();
	private boolean logged = false;
	private HttpParser parser;
	private HTTPProxyLogger logger = HTTPProxyLogger.getInstance();

	public HttpSelectorProtocolAdmin(int bufSize) {
		parser = new HttpParser();

	}

	public void handleAccept(SocketChannel admClntChan) throws IOException {
		// logger.info("New admin connected");
		list.put(admClntChan, new ChannelBuffers());
	}

	public SocketChannel handleRead(SelectionKey key) throws IOException {
		SocketChannel s = (SocketChannel) key.channel();
		ChannelBuffers channelBuffers = list.get(s);
		int bytesRead = s.read(channelBuffers.getBuffer(BufferType.read));
		if (bytesRead > 0) {
			try {
				PDCRequest request;

				request = (PDCRequest) parser.parse(
						channelBuffers.getBuffer(BufferType.read),
						channelBuffers.getRequest());
				if (request != null && request.getState() != ParsingState.Body) {
					return null;
				}
				PDCResponse response = request.parseMessage();
				if (response != null) {
					String resp = response.getVersion() + " "
							+ Integer.toString(response.getCode()) + " "
							+ response.getVerboseCode() + '\n'
							+ response.getBody() + '\n';
					s.write(ByteBuffer.wrap(resp.getBytes()));
					s.write(ByteBuffer.wrap(response.getData().getBytes()));

					list.put(s, new ChannelBuffers());
					channelBuffers.setRequest(new PDCRequest());
				}
			} catch (BadSyntaxException e) {
				logger.info("[AdminHandler] Command Error");
				s.write(ByteBuffer.wrap("Error Admin Handler. Disconnected\n"
						.getBytes()));
				s.close();
				key.cancel();
				return null;

			} catch (Exception e) {
				logger.info("[AdminHandler] Unexpected Error. Lost connection with the admin");
				s.close();
				key.cancel();
				return null;
			}
		}

		return s;

	}

	public void handleWrite(SelectionKey key) throws IOException {
		SocketChannel s = (SocketChannel) key.channel();
		ByteBuffer wrBuffer = list.get(s).getBuffer(BufferType.write);
		wrBuffer.flip();
		s.write(wrBuffer);
		wrBuffer.compact();
		s.register(key.selector(), SelectionKey.OP_READ); // receiver
		key.interestOps(SelectionKey.OP_READ);
	}
}
