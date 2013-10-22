package ar.edu.itba.pdc.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import javax.xml.transform.URIResolver;

import ar.edu.itba.pdc.parser.HTTPRequest;
import ar.edu.itba.pdc.parser.HttpParser;

public class HttpSelectorProtocol implements TCPProtocol {
	private int bufSize; // Size of I/O buffer

	public HttpSelectorProtocol(int bufSize) {
		this.bufSize = bufSize;
	}

	public void handleAccept(SelectionKey key) throws IOException {
		SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
		clntChan.configureBlocking(false); // Must be nonblocking to register
		// Register the selector with new channel for read and attach byte
		// buffer
		clntChan.register(key.selector(), SelectionKey.OP_READ,
				ByteBuffer.allocate(bufSize));
	}

	public void handleRead(SelectionKey key) throws IOException {
		// Client socket channel has pending data
		SocketChannel clntChan = (SocketChannel) key.channel();
		ByteBuffer buf = (ByteBuffer) key.attachment();
		long bytesRead = clntChan.read(buf);
		if (bytesRead == -1) { // Did the other end close?
			clntChan.close();
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
		ByteBuffer buf = (ByteBuffer) key.attachment();
		HttpParser parser = HttpParser.getInstance();
		HTTPRequest httpheaders = parser.parseHeaders(buf);
//		System.out.println("URI: " + httpheaders.getURI());
//		System.out.println("Version: " + httpheaders.getVersion());
//		System.out.println("Method: " + httpheaders.getHttpmethod());
//		System.out.println("Extra headers:" + httpheaders.getHeaders());
		
		/* ----------- CONEXION A SERVIDOR DESTINO ----------- */
		URI uri = URI.create(httpheaders.getURI());
		SocketChannel responseChannel = SocketChannel.open();
		responseChannel.configureBlocking(false);
		responseChannel.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(bufSize));

		int port = uri.getPort() == -1 ? 80 : uri.getPort();
		if (!responseChannel.connect(new InetSocketAddress(uri.getHost(), port))) {
			while (!responseChannel.finishConnect()) {
				System.out.print(".");
			}
		}
		
//		buf.flip(); // Prepare buffer for writing
//		// SocketChannel clntChan = (SocketChannel) key.channel();
//		SocketChannel dumpChan = SocketChannel.open();
//		dumpChan.configureBlocking(false);
//		if (!dumpChan.connect(new InetSocketAddress("localhost", 9091))) {
//			while (!dumpChan.finishConnect()) {
//				System.out.println("conectando"); // Do someth-ing else
//			}
//		}
//		b.flip();
//		dumpChan.write(b);
//		//dumpChan.write(buf);
//		// clntChan.write(buf);
//		if (!b.hasRemaining()) { // Buffer completely written?
//			// Nothing left, so no longer interested in writes
//			key.interestOps(SelectionKey.OP_READ);
//		}
//		b.compact(); // Make room for more data to be read in
	}
}
