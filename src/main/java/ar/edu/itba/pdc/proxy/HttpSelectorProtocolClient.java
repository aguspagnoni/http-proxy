package ar.edu.itba.pdc.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

import ar.edu.itba.pdc.parser.HttpRequest;
import ar.edu.itba.pdc.parser.HttpResponse;
import ar.edu.itba.pdc.parser.Message;

public class HttpSelectorProtocolClient implements TCPProtocol {

	private HashMap<SocketChannel, ProxyConnection> proxyconnections = new HashMap<SocketChannel, ProxyConnection>();

	public HttpSelectorProtocolClient(int bufSize) {
	}

	public void handleAccept(SocketChannel channel) throws IOException {
		ProxyConnection conn = new ProxyConnection();
		conn.setClient(channel);
		proxyconnections.put(channel, conn);
		
	}

	public SocketChannel handleRead(SelectionKey key) throws IOException {
		// Client socket channel has pending data
		SocketChannel clientChannel = (SocketChannel) key.channel();
		ProxyConnection conn = proxyconnections.get(clientChannel);

		ByteBuffer buf = conn.getBuffer(clientChannel);
		long bytesRead = clientChannel.read(buf);
		if (bytesRead == -1) { // Did the other end close?
			clientChannel.close(); // ACA HAY QUE TENER EN CUENTA QUE SI LA CERRO EL
								// SERVER, DEVOLVERIA MENOS 1, DESATTACHEARLO Y
								// DECIRLE UQE NO TIENE CONEXION ASOCIADA ASI
								// TIEN QUE CREAR UNA NUEVA YA QUE EL CLIENTE SE
								// TIENTA A SEGUIR MANDANDO REQUESTS PORQUE
								// TIENE UNA CONEXION ABIERTA, SI MURIO EL
								// CLIENTE MATAR TODO.
		} else if (bytesRead > 0) {
			// Indicate via key that reading/writing are both of interest now.
			key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		}
		return clientChannel;
	}

	public void handleWrite(SelectionKey key) throws IOException {
		/*
		 * Channel is available for writing, and key is valid (i.e., client
		 * channel not closed).
		 */
		// Retrieve data read earlier

		/* ----------- PARSEO DE REQ/RESP ----------- */
		ProxyConnection conn = (ProxyConnection) key.attachment();
		SocketChannel channel = (SocketChannel) key.channel();
		// TODO ver de poner el parser no como singleton sino como clase que
		// quede en el proxyconnection.. POR CONCURRENCIA.

		Message message = conn.getMessage(channel);

		/* ----------- CONEXION A SERVIDOR DESTINO ----------- */
		SocketChannel serverchannel;
		if ((serverchannel = conn.getServer()) == null) {
			URL uri = new URL(((HttpRequest) message).getURI());
			// URI uri = URI.create((());

			serverchannel = SocketChannel.open();
			serverchannel.configureBlocking(false);
			serverchannel.register(key.selector(), SelectionKey.OP_READ, conn);

			int port = uri.getPort() == -1 ? 80 : uri.getPort();
			if (!serverchannel.connect(new InetSocketAddress(uri.getHost(),
					port))) {
				while (!serverchannel.finishConnect()) {
					System.out.print(".");
				}
			}
			conn.setServer(serverchannel);
		}

		SocketChannel receiver = conn.getOppositeChannel(channel);
		ByteBuffer buf = conn.getBuffer(channel);
		int byteswritten = 0;
		boolean hasRemaining = true;
		buf.flip(); // Prepare buffer for writing
		String content = new String(buf.array()).substring(0,
				buf.array().length);
		System.out.println(content);
		HttpResponse r = new HttpResponse();
		byteswritten = receiver.write(buf);
		hasRemaining = buf.hasRemaining(); // Buffer completely written?
		buf.compact(); // Make room for more data to be read in
		SocketChannel opposite = conn.getOppositeChannel(channel);
		opposite.register(key.selector(), SelectionKey.OP_READ, conn); // receiver
		key.interestOps(SelectionKey.OP_READ); // Sender has finished
												// writing

	}

}

// System.out.println("URI: " + httpheaders.getURI());
// System.out.println("Version: " + httpheaders.getVersion());
// System.out.println("Method: " + httpheaders.getHttpmethod());
// System.out.println("Extra headers:" + httpheaders.getHeaders());
