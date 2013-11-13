package ar.edu.itba.pdc.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.HashMap;

import ar.edu.itba.pdc.filters.StatisticsFilter;
import ar.edu.itba.pdc.logger.HTTPProxyLogger;
import ar.edu.itba.pdc.parser.HttpRequest;
import ar.edu.itba.pdc.parser.HttpResponse;
import ar.edu.itba.pdc.parser.Message;
import ar.edu.itba.pdc.parser.enumerations.ParsingState;

public class HttpSelectorProtocolClient implements TCPProtocol {

	private HashMap<SocketChannel, ProxyConnection> proxyconnections = new HashMap<SocketChannel, ProxyConnection>();
	private HTTPProxyLogger logger = HTTPProxyLogger.getInstance();

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
			// System.out.println(new String(buf.array(), 0, 100));
		} catch (IOException e) {
			System.out.println("\nfallo el read");
			e.printStackTrace();
			return null;
		}

		logger.info("\n[READ] " + bytesRead + " from "
				+ channel.socket().getInetAddress() + ":"
				+ channel.socket().getPort());
		SocketChannel server = conn.getServer();
		/* ----------- OS SEND CLOSE ----------- */
		if (bytesRead == -1) {
			logger.info("\n[SENT CLOSE] cliente "
					+ channel.socket().getInetAddress() + ":"
					+ channel.socket().getPort());
			if (!conn.isClient(channel)) { // is server remove from connections
				proxyconnections.remove(channel);
				conn.resetServer();
			}
			channel.close();
			key.cancel();
			conn.resetIncompleteMessage();
		}
		/* ----------- SOMETHING TO READ ----------- */
		if (bytesRead > 0) {

			/* ----------- PARSEO DE REQ/RESP ----------- */

			Message message = conn.getMessage(channel);
			message.increaseAmountRead((int) bytesRead);
			message.setFrom(channel.socket().getInetAddress().toString());

			SocketChannel serverchannel = server;
			if (conn.isClient(channel)
					&& message.getState() != ParsingState.Body) {
				return null;
			}

			/* ----------- CONEXION A SERVIDOR DESTINO ----------- */
			try {
				serverchannel = connectToRemoteServer(conn, message,
						serverchannel);
			} catch (UnresolvedAddressException e) { // TODO HACERLO DE LA FORMA
														// BIEN. AGARRANDOLO DE
														// UN ARCHIVO
				String notFound = "HTTP/1.1 404 BAD REQUEST\r\n\r\n<html><body>404 Not Found<br><br>This may be a DNS problem or the page you were looking for doesn't exist.</body></html>\r\n";
				generateResponse(channel, notFound);
				return null;

			}

//			StatisticsFilter.getInstance().filter(message);

			// Indicate via key that reading/writing are both of interest now.
			key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
			if (channel.isOpen())
				channel.register(key.selector(), SelectionKey.OP_WRITE);
			return serverchannel;
		}

		return null;
	}

	private void generateResponse(SocketChannel channel, String notFound)
			throws IOException {
		ByteBuffer resp = ByteBuffer.allocate(notFound.length());
		resp.put(notFound.getBytes());
		resp.flip();
		channel.write(resp);
		channel.close();
		proxyconnections.remove(channel);
	}

	private SocketChannel connectToRemoteServer(ProxyConnection conn,
			Message message, SocketChannel serverchannel)
			throws MalformedURLException, IOException {
		if (serverchannel == null) {
			String url = null;
			if (((HttpRequest) message).getURI().startsWith("/"))
				url = ((HttpRequest) message).getHeaders().get("host")
						+ ((HttpRequest) message).getURI();
			else
				url = ((HttpRequest) message).getURI();
			String[] splitted = url.split("http://");
			url = "http://"
					+ (splitted.length >= 2 ? splitted[1] : splitted[0]);
			URL uri = new URL(url);

			serverchannel = SocketChannel.open();
			serverchannel.configureBlocking(false);

			int port = uri.getPort() == -1 ? 80 : uri.getPort();

			if (!serverchannel.connect(new InetSocketAddress(uri.getHost(),
					port))) {
				while (!serverchannel.finishConnect())
					;
				// System.out.println();
			}

			conn.setServer(serverchannel);
			proxyconnections.put(serverchannel, conn);
		}
		return serverchannel;
	}

	public void handleWrite(SelectionKey key) throws IOException {

		SocketChannel channel = (SocketChannel) key.channel();
		ProxyConnection conn = proxyconnections.get(channel);
		SocketChannel receiver = conn.getOppositeChannel(channel);
		ByteBuffer buf = conn.getBuffer(channel);
		
		if (receiver == null || !receiver.isOpen())
			return;

		Message message = conn.getIncompleteMessage();
		ByteBuffer pHeadersBuf;
		int byteswritten = 0;

		if (message != null
				&& (pHeadersBuf = message.getPartialHeadersBuffer()) != null) { // Headers
																				// came
																				// in
																				// different
																				// reads.
			pHeadersBuf.flip();
			try {
				receiver.write(pHeadersBuf);
			} catch (ClosedChannelException e) {
				System.out
						.println("AAAAAAAALTA ESESIO TIRO EL GUACHIN GATO ESE");
			}
			message.finishWithLeftHeaders();
		}
		byteswritten = receiver.write(buf);

		logger.info("\n[WRITE] " + byteswritten + " to "
				+ receiver.socket().getInetAddress() + ":"
				+ receiver.socket().getPort());

		buf.compact(); // Make room for more data to be read in
		receiver.register(key.selector(), SelectionKey.OP_READ, conn); // receiver
																		// will
																		// write
																		// us
																		// back
		key.interestOps(SelectionKey.OP_READ); // Sender has finished writing

	}
}
