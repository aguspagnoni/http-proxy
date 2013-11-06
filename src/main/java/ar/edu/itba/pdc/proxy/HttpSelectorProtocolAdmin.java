package ar.edu.itba.pdc.proxy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import ar.edu.itba.pdc.exceptions.BadSyntaxException;
import ar.edu.itba.pdc.parser.StupidAdminParser;

public class HttpSelectorProtocolAdmin implements TCPProtocol {

	private Map<SocketChannel, ChannelBuffers> list = new HashMap<SocketChannel, ChannelBuffers>();
	private boolean logged = false;
	private StupidAdminParser parser;

	public HttpSelectorProtocolAdmin(int bufSize) {
		parser = new StupidAdminParser();

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
			String response;
			if ((response = parser.parseCommand(
					channelBuffers.getBuffer(BufferType.read), bytesRead)) != null) {
				if (logged || response.equals("PASSWORD OK\n")) {
					logged = true;
					s.write(ByteBuffer.wrap(response.getBytes()));
				} else if (response.equals("INVALID PASSWORD\n")) {
					s.write(ByteBuffer.wrap(response.getBytes()));
				} else {
					s.write(ByteBuffer.wrap("Not logged in!\n".getBytes()));
				}
			}
		} catch (BadSyntaxException e) {
			System.out.println("Bad syntax");
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
