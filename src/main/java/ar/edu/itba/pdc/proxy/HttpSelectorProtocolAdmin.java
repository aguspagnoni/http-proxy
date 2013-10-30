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

public class HttpSelectorProtocolAdmin implements TCPProtocol {

	private HashMap<SocketChannel, ProxyConnection> proxyconnections = new HashMap<SocketChannel, ProxyConnection>();
	private boolean logged = false;

	public HttpSelectorProtocolAdmin(int bufSize) {
	}

	public void handleAccept(SelectionKey key) throws IOException {
		SocketChannel adminChannel = ((ServerSocketChannel) key.channel()).accept();
		adminChannel.configureBlocking(false); // Must be nonblocking to register
		// Register the selector with new channel for read and attach byte
		// buffer
		ProxyConnection conn = new ProxyConnection();
		conn.setClient(adminChannel);
		proxyconnections.put(adminChannel, conn);
		adminChannel.register(key.selector(), SelectionKey.OP_READ, conn);
	}

	public void handleRead(SelectionKey key) throws IOException {
		// Client socket channel has pending data
		SocketChannel adminChannel = (SocketChannel) key.channel();
		ProxyConnection conn = (ProxyConnection) key.attachment();

		ByteBuffer buf = conn.getBuffer(adminChannel);

		int bytesRead = adminChannel.read(buf);
		String fullCommand = new String(buf.array()).substring(0,
				bytesRead);
		if (bytesRead == -1) { // Did the other end close?
			adminChannel.close(); // ACA HAY QUE TENER EN CUENTA QUE SI LA CERRO EL SERVER, DEVOLVERIA MENOS 1, DESATTACHEARLO Y DECIRLE UQE NO TIENE CONEXION ASOCIADA ASI TIEN QUE CREAR UNA NUEVA YA QUE EL CLIENTE SE TIENTA A SEGUIR MANDANDO REQUESTS PORQUE TIENE UNA CONEXION ABIERTA, SI MURIO EL CLIENTE MATAR TODO.
		} else if (bytesRead > 0) {
			// Indicate via key that reading/writing are both of interest now.
			key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
			
		}
		System.out.println(fullCommand);

	}

	public void handleWrite(SelectionKey key) throws IOException {
		/*
		 * Channel is available for writing, and key is valid (i.e., client
		 * channel not closed).
		 */
		// Retrieve data read earlier
		
		/* ----------- PARSEO DE REQ/RESP ----------- */
		ProxyConnection conn = (ProxyConnection) key.attachment();
		SocketChannel adminChannel = (SocketChannel) key.channel();
		// TODO ver de poner el parser no como singleton sino como clase que
		// quede en el proxyconnection.. POR CONCURRENCIA.
		
		Message message = conn.getMessage(adminChannel);

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
		if (conn.handleWrite(adminChannel)) {
			SocketChannel receiver = conn.getOppositeChannel(adminChannel);
			receiver.register(key.selector(), SelectionKey.OP_READ, conn); // receiver channel has something to write now
			key.interestOps(SelectionKey.OP_READ); // Sender has finished writing
		}
	}
}
