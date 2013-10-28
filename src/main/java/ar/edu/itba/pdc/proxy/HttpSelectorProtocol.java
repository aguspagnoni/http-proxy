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
	
	private static final int TIMEOUT = 5000; // Wait timeout (milliseconds)

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
		long bytesRead = 0;
		try {
			bytesRead = channel.read(buf);
			if (bytesRead == 0)
				System.out.println("que onda loocooo");
		} catch (IOException e) {
			System.out.println("\nfallo el read");
			return;
		}
		System.out.println("\nse leyeron : "+ bytesRead);
		conn.setBytesRead(bytesRead);
		if (bytesRead == -1) { // Did the other end close?
			if (conn.isClient(channel)) {
				System.out.println("\n[SENT CLOSE] en el proxy "+channel.socket().getLocalAddress()+":"+channel.socket().getLocalPort() + "en el chrome " + channel.socket().getInetAddress()+":"+channel.socket().getPort());
				if (conn.getServer() != null)
					conn.getServer().close(); // close the server channel
				channel.close();
				key.attach(null); // de-reference the proxy connection as it is no longer useful
			} else {
				System.out.println("\n[SENT CLOSE] en el proxy "+channel.socket().getLocalAddress()+":"+channel.socket().getLocalPort() + "en el servidor remoto " + channel.socket().getInetAddress()+":"+channel.socket().getPort());
				conn.getServer().close();
				conn.resetServer(); 
			}
			
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
		
		/* ----------- ESTADO DE CONEXION ----------- */
		ProxyConnection conn = (ProxyConnection) key.attachment();
		
		SocketChannel channel = (SocketChannel) key.channel();
		
		/* ----------- PARSEO DE REQ/RESP ----------- */
		Message message = conn.getMessage(channel);

		/* ----------- CONEXION A SERVIDOR DESTINO ----------- */
		SocketChannel serverchannel;
		if (message != null && (serverchannel = conn.getServer()) == null) { // message is null when incomplete
			String url = null;
			if (((HttpRequest) message).getURI().startsWith("/"))
				url =  ((HttpRequest) message).getHeaders().get("host") + ((HttpRequest) message).getURI();
			else
				url = ((HttpRequest) message).getURI();
			String[] splitted = url.split("http://");
			url = "http://" + (splitted.length == 2 ? splitted[1] : splitted[0]);
			URL uri = new URL(url);
			
			serverchannel = SocketChannel.open();
			serverchannel.configureBlocking(false);
			serverchannel.register(key.selector(), SelectionKey.OP_READ, conn);

			int port = uri.getPort() == -1 ? 80 : uri.getPort();
			boolean timeout = false;
			if (!serverchannel.connect(new InetSocketAddress(uri.getHost(),
					port))) {
				try {
					long ini = System.currentTimeMillis();
					while (!serverchannel.finishConnect() && !timeout) {
						timeout = TIMEOUT < System.currentTimeMillis() - ini;
						System.out.print(".");
					}
					if (timeout)
						System.out.println("tiempo de espera agotado.");
				} catch (Exception e) {
					System.out.println("no se pudo terminar de conectar. probablemente un connection refused");
					return;
				}
			}
			if (!timeout)
				conn.setServer(serverchannel);
		}
		if (message == null)
			key.interestOps(SelectionKey.OP_WRITE); // keep reading
		else if (channel != null && conn.getServer() != null && conn.handleWrite(channel)) { // message null means it's not a complete message
			SocketChannel receiver = conn.getOppositeChannel(channel);
			receiver.register(key.selector(), SelectionKey.OP_READ, conn); // receiver channel has something to write now
			key.interestOps(SelectionKey.OP_READ); // Sender has finished writing
		}
	}
}