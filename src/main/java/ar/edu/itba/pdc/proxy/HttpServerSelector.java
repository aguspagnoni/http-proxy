package ar.edu.itba.pdc.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ar.edu.itba.pdc.configuration.PortConfiguration;
import ar.edu.itba.pdc.logger.HTTPProxyLogger;

public class HttpServerSelector {
	private static final int BUFSIZE = 256; // Buffer size (bytes)
	private static final int TIMEOUT = 3000; // Wait timeout (milliseconds)
	private Map<AbstractSelectableChannel, TCPProtocol> handlerMap = new HashMap<AbstractSelectableChannel, TCPProtocol>();
	private HTTPProxyLogger logger= HTTPProxyLogger.getInstance();
	
	public void run() throws IOException {

		/* Create handlers */
		HttpSelectorProtocolClient clientHandler = new HttpSelectorProtocolClient(
				BUFSIZE);
		HttpSelectorProtocolAdmin adminHandler = new HttpSelectorProtocolAdmin(
				BUFSIZE);

		Selector selector = Selector.open();
		PortConfiguration config = new PortConfiguration();

		/* Bind HTTP Client Socket */
		ServerSocketChannel clientChannel = ServerSocketChannel.open();
		clientChannel.socket().bind(
				new InetSocketAddress(config.getPortClient()));
		clientChannel.configureBlocking(false);
		clientChannel.register(selector, SelectionKey.OP_ACCEPT);
		handlerMap.put(clientChannel, clientHandler);
		/* Bind Administrator Socket */
		ServerSocketChannel adminChannel = ServerSocketChannel.open();
		adminChannel.socket()
				.bind(new InetSocketAddress(config.getPortAdmin()));
		adminChannel.configureBlocking(false);
		adminChannel.register(selector, SelectionKey.OP_ACCEPT);
		handlerMap.put(adminChannel, adminHandler);

		/* Create Protocol  */
		// TCPProtocol protocol = new HttpSelectorProtocol(BUFSIZE);
		while (!Thread.interrupted()) {
			if (selector.select(TIMEOUT) == 0) {
				System.out.print(".");
				continue;
			}
			Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
			while (keyIter.hasNext()) {
				SelectionKey key = keyIter.next();
				if (key.isValid() && key.isAcceptable()) {
					SocketChannel newChannel = ((ServerSocketChannel) key
							.channel()).accept();
					newChannel.configureBlocking(false);
					newChannel.register(selector, SelectionKey.OP_READ);
					TCPProtocol handler = handlerMap.get(key.channel());
					handlerMap.put(newChannel, handler);
					handler.handleAccept(newChannel);
					logger.info("\n[ACCEPT] cliente "
							+ newChannel.socket().getInetAddress() + ":"
							+ newChannel.socket().getPort());


					// protocol.handleAccept(key);
				}
				if (key.isValid() && key.isReadable()) {
					TCPProtocol handler = handlerMap.get(key.channel());
					if (handler != null) {// for rare connections
						SocketChannel channel = handler.handleRead(key);
						if (channel != null) {
							handlerMap.put(channel, handlerMap.get(key.channel()));
						}
					}
				}
				if (key.isValid() && key.isWritable()) {
					handlerMap.get(key.channel()).handleWrite(key);
				}
				keyIter.remove();
			}
		}
	}
}
