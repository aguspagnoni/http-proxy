package ar.edu.itba.pdc.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

import ar.edu.itba.pdc.logger.HTTPProxyLogger;
import ar.edu.itba.pdc.parser.HttpRequest;
import ar.edu.itba.pdc.parser.Message;

public class HttpSelectorProtocolClient implements TCPProtocol {

	private HashMap<SocketChannel, ProxyConnection> proxyconnections = new HashMap<SocketChannel, ProxyConnection>();
	private HTTPProxyLogger logger= HTTPProxyLogger.getInstance();
	
	public HttpSelectorProtocolClient(int bufSize) {
	}

	public void handleAccept(SocketChannel channel) throws IOException {
		ProxyConnection conn = new ProxyConnection();
		conn.setClient(channel);
		proxyconnections.put(channel, conn);

	}

	public SocketChannel handleRead(SelectionKey key) throws IOException {
		// Client socket channel has pending data
		SocketChannel channel = (SocketChannel) key.channel();
		ProxyConnection conn = proxyconnections.get(channel);

		ByteBuffer buf = conn.getBuffer(channel);
		long bytesRead = 0;
		try {
			bytesRead = channel.read(buf);
			System.out.println(new String(buf.array(), 0, 100));
		} catch (IOException e) {
			System.out.println("\nfallo el read");
			return null;
		}
		System.out.println("\n[READ] " + bytesRead + " from "
				+ channel.socket().getInetAddress() + ":"
				+ channel.socket().getPort());
		// conn.setBytesRead(bytesRead);
		if (bytesRead == -1) { // Did the other end close?
			if (conn.isClient(channel)) {
				logger.info("\n[SENT CLOSE] cliente "
						+ channel.socket().getInetAddress() + ":"
						+ channel.socket().getPort());
//				System.out.println("\n[SENT CLOSE] cliente "
//						+ channel.socket().getInetAddress() + ":"
//						+ channel.socket().getPort());
				if (conn.getServer() != null)
					conn.getServer().close(); // close the server channel
				channel.close();
				key.attach(null); // de-reference the proxy connection as it is
									// no longer useful
				return null;
			} else {
				logger.info("\n[SENT CLOSE] servidor remoto "
						+ channel.socket().getInetAddress() + ":"
						+ channel.socket().getPort());
//				System.out.println("\n[SENT CLOSE] servidor remoto "
//						+ channel.socket().getInetAddress() + ":"
//						+ channel.socket().getPort());
				conn.getServer().close();
				// proxyconnections.remove(channel);
				// key.cancel();
				conn.resetIncompleteMessage();
				conn.resetServer();
			}

		} else if (bytesRead > 0) {
			// Indicate via key that reading/writing are both of interest now.
			Message message = conn.getMessage(channel);
			message.increaseAmountRead((int) bytesRead); // DECIDIR SI INT O
			message.setFrom("client" + channel.socket().getInetAddress()); // LONG

			/* ----------- CONEXION A SERVIDOR DESTINO ----------- */
			SocketChannel serverchannel;
			if ((serverchannel = conn.getServer()) == null) {
				String url = null;
				if (((HttpRequest) message).getURI().startsWith("/"))
					url = ((HttpRequest) message).getHeaders().get("host")
							+ ((HttpRequest) message).getURI();
				else
					url = ((HttpRequest) message).getURI();
				String[] splitted = url.split("http://");
				url = "http://"
						+ (splitted.length == 2 ? splitted[1] : splitted[0]);
				URL uri = new URL(url);

				serverchannel = SocketChannel.open();
				serverchannel.configureBlocking(false);

				int port = uri.getPort() == -1 ? 80 : uri.getPort();
				if (!serverchannel.connect(new InetSocketAddress(uri.getHost(),
						port))) {
					// if (!serverchannel.connect(new
					// InetSocketAddress("localhost",
					// 8888))) {
					while (!serverchannel.finishConnect()) {
						System.out.print("*");
					}
				}
				// serverchannel.register(key.selector(),
				// SelectionKey.OP_WRITE);
				conn.setServer(serverchannel);
				proxyconnections.put(serverchannel, conn);
			}
			key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
			return serverchannel;
		}

		return null;
	}

	public void handleWrite(SelectionKey key) throws IOException {
		/*
		 * Channel is available for writing, and key is valid (i.e., client
		 * channel not closed).
		 */

		// Retrieve data read earlier
		SocketChannel channel = (SocketChannel) key.channel();

		/* ----------- PARSEO DE REQ/RESP ----------- */
		ProxyConnection conn = proxyconnections.get(channel);

		SocketChannel receiver = conn.getOppositeChannel(channel);
		ByteBuffer buf = conn.getBuffer(channel);

		int byteswritten = 0;
		boolean hasRemaining = true;
		// buf.flip(); // Prepare buffer for writing
		System.out.println(new String(buf.array(), 0, 100));

		byteswritten = receiver.write(buf);
		Message m = conn.getMessage(channel);
		m.increaseAmountRead(byteswritten);
		conn.handleFilters(m);
		hasRemaining = buf.hasRemaining(); // Buffer completely written?
		logger.info("\n[WRITE] " + byteswritten + " to "
				+ receiver.socket().getInetAddress() + ":"
				+ receiver.socket().getPort());


		buf.compact(); // Make room for more data to be read in
		receiver.register(key.selector(), SelectionKey.OP_READ, conn); // receiver
		key.interestOps(SelectionKey.OP_READ); // Sender has finished
												// writing

	}

}

// System.out.println("URI: " + httpheaders.getURI());
// System.out.println("Version: " + httpheaders.getVersion());
// System.out.println("Method: " + httpheaders.getHttpmethod());
// System.out.println("Extra headers:" + httpheaders.getHeaders());
