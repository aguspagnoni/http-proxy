package ar.edu.itba.pdc.proxy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import ar.edu.itba.pdc.exceptions.BadSyntaxException;
import ar.edu.itba.pdc.logger.HTTPProxyLogger;
import ar.edu.itba.pdc.parser.AdminParser;
import ar.edu.itba.pdc.parser.PDCRequest;
import ar.edu.itba.pdc.parser.PDCResponse;

public class HttpSelectorProtocolAdmin implements TCPProtocol {

	private Map<SocketChannel, ChannelBuffers> list = new HashMap<SocketChannel, ChannelBuffers>();
	private boolean logged = false;
	private AdminParser parser;
	private HTTPProxyLogger logger = HTTPProxyLogger.getInstance();

	public HttpSelectorProtocolAdmin(int bufSize) {
		parser = new AdminParser();

	}

	public void handleAccept(SocketChannel admClntChan) throws IOException {
		// logger.info("New admin connected");
		list.put(admClntChan, new ChannelBuffers());
	}

	public SocketChannel handleRead(SelectionKey key) throws IOException {
		// Client socket channel has pending data
		SocketChannel s = (SocketChannel) key.channel();
		ChannelBuffers channelBuffers = list.get(s);
		int bytesRead = s.read(channelBuffers.getBuffer(BufferType.read));
		try {
			PDCResponse response;
			if ((response = (PDCResponse) parser
					.parse(channelBuffers.getBuffer(BufferType.read),
							channelBuffers.getRequest())) != null) {
				if (logged || response.equals("PASSWORD OK\n")) {
					if (logged && response.equals("PASSWORD OK\n")) {
						response = null;
						// response = "ALREADY LOGGED\n";
					}

					logged = true;
					s.write(ByteBuffer.wrap(response.getBytes()));
				} else if (response.equals("INVALID PASSWORD\n")) {
					if (logged) {
						response = null;
						// response = "ALREADY LOGGED\n";
					}
					s.write(ByteBuffer.wrap(response.getBytes()));
				} else {
					s.write(ByteBuffer.wrap("Not logged in!\n".getBytes()));
				}
			}
		} catch (BadSyntaxException e) {
			logger.info("Bad syntax");

			s.write(ByteBuffer.wrap("BAD SYNTAX\n".getBytes()));
		} catch (Exception e) {
			logged = false;
			// logger.error("Lost connection with the admin");
			s.close();
			key.cancel();
			return null;
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
