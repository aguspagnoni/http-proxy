package ar.edu.itba.pdc.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

import ar.edu.itba.pdc.configuration.PortConfiguration;

public class HttpServerSelector {
	private static final int BUFSIZE = 256; // Buffer size (bytes)
	private static final int TIMEOUT = 3000; // Wait timeout (milliseconds)

	public static void main(String[] args) throws IOException {
		Selector selector = Selector.open();
		PortConfiguration config = new PortConfiguration();

		/* Bind HTTP Client Socket */
		ServerSocketChannel clientChannel = ServerSocketChannel.open();
		clientChannel.socket().bind(
				new InetSocketAddress(config.getPortClient()));
		clientChannel.configureBlocking(false);
		clientChannel.register(selector, SelectionKey.OP_ACCEPT);

		/* Bind Administrator Socket */
		ServerSocketChannel adminChannel = ServerSocketChannel.open();
		adminChannel.socket()
				.bind(new InetSocketAddress(config.getPortAdmin()));
		adminChannel.configureBlocking(false);
		adminChannel.register(selector, SelectionKey.OP_ACCEPT);

		/* Create Protocol and runnnnn */
		TCPProtocol protocol = new HttpSelectorProtocol(BUFSIZE);
		while (!Thread.interrupted()) {
			if (selector.select(TIMEOUT) == 0) {
				System.out.print(".");
				continue;
			}
			Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
			while (keyIter.hasNext()) {
				SelectionKey key = keyIter.next();
				if (key.isAcceptable()) {
					protocol.handleAccept(key);
				}
				if (key.isReadable()) {
					protocol.handleRead(key);
				}
				if (key.isValid() && key.isWritable()) {
					protocol.handleWrite(key);
				}
				keyIter.remove();
			}
		}
	}
}
