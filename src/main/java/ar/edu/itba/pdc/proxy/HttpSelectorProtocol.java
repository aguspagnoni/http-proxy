package ar.edu.itba.pdc.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

import ar.edu.itba.pdc.parser.HttpRequest;
import ar.edu.itba.pdc.parser.Message;

public class HttpSelectorProtocol implements TCPProtocol {

	private HashMap<SocketChannel, ProxyConnection> proxyconnections = new HashMap<SocketChannel, ProxyConnection>();

	public HttpSelectorProtocol(int bufSize) {
	}

	public void handleAccept(SelectionKey key) throws IOException {
		SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
		clntChan.configureBlocking(false); // Must be nonblocking to register
		// Register the selector with new channel for read and attach byte
		// buffer

		ProxyConnection conn = new ProxyConnection();
		conn.setClient(clntChan);
		proxyconnections.put(clntChan, conn);
		clntChan.register(key.selector(), SelectionKey.OP_READ, conn);
	}

	public void handleRead(SelectionKey key) throws IOException {
		// Client socket channel has pending data
		SocketChannel channel = (SocketChannel) key.channel();
		ProxyConnection conn = (ProxyConnection) key.attachment();

		ByteBuffer buf = conn.getBuffer(channel);
		long bytesRead = channel.read(buf);
		if (bytesRead == -1) { // Did the other end close?
			channel.close(); // ACA HAY QUE TENER EN CUENTA QUE SI LA CERRO EL SERVER, DEVOLVERIA MENOS 1, DESATTACHEARLO Y DECIRLE UQE NO TIENE CONEXION ASOCIADA ASI TIEN QUE CREAR UNA NUEVA YA QUE EL CLIENTE SE TIENTA A SEGUIR MANDANDO REQUESTS PORQUE TIENE UNA CONEXION ABIERTA, SI MURIO EL CLIENTE MATAR TODO.
		} else if (bytesRead > 0) {
			// Indicate via key that reading/writing are both of interest now.
			key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		}
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
//			URI uri = URI.create((());
			
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
//		if (conn.handleWrite(channel, Integer.valueOf(((HttpRequest) message).getHeaders().get("content-length")))) {
		if (conn.handleWrite(channel, 1)) {
			SocketChannel receiver = conn.getOppositeChannel(channel);
			receiver.register(key.selector(), SelectionKey.OP_READ, conn); // receiver channel has something to write now
			key.interestOps(SelectionKey.OP_READ); // Sender has finished writing
		}
	}
}

// System.out.println("URI: " + httpheaders.getURI());
// System.out.println("Version: " + httpheaders.getVersion());
// System.out.println("Method: " + httpheaders.getHttpmethod());
// System.out.println("Extra headers:" + httpheaders.getHeaders());
