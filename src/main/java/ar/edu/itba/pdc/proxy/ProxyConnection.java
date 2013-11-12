package ar.edu.itba.pdc.proxy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import ar.edu.itba.pdc.filters.Filter;
import ar.edu.itba.pdc.filters.StatisticsFilter;
import ar.edu.itba.pdc.filters.TransformationFilter;
import ar.edu.itba.pdc.parser.HttpParser;
import ar.edu.itba.pdc.parser.HttpRequest;
import ar.edu.itba.pdc.parser.HttpResponse;
import ar.edu.itba.pdc.parser.InvalidMessageException;
import ar.edu.itba.pdc.parser.Message;

public class ProxyConnection {

	private int bufSize = 8192; // Size of I/O buffer
	private SocketChannel server;
	private SocketChannel client;

	private int serverport;
	private int clientport;
	private String serveraddr;
	private String clientaddr;

	private ByteBuffer serverbuf;
	private ByteBuffer clientbuf;

	private Message incompleteMessage;

	private HttpParser parser = new HttpParser();
	private List<Filter> filterList = new ArrayList<Filter>();

	public ProxyConnection() {
		initialize();
	}

	private void initialize() {
		filterList.add(StatisticsFilter.getInstance());
		filterList.add(TransformationFilter.getInstance());
	}

	public Message getMessage(SocketChannel sender) {
		ByteBuffer buf = getBuffer(sender);
		Message message = null;
		try {
			if (isClient(sender)) {
				if (incompleteMessage == null || incompleteMessage.getClass().equals(HttpResponse.class))
					incompleteMessage = new HttpRequest();
				message = (HttpRequest) parser.parse(buf,
						(HttpRequest) incompleteMessage);
			} else {
				if (incompleteMessage == null || incompleteMessage.getClass().equals(HttpRequest.class))
					incompleteMessage = new HttpResponse();
				message = (HttpResponse) parser.parse(buf, incompleteMessage);
			}

		} catch (InvalidMessageException e) {
			System.out.println(e);
		}

		if (message != null && !message.hasHeadersBuffered() && message.isFinished()) {
			resetIncompleteMessage();
			return message;
		} else
			incompleteMessage = message;
		return message;
	}

	public void handleFilters(Message m) throws IOException {

		// SocketChannel receiver = getOppositeChannel(sender);
		// ByteBuffer buf = getBuffer(sender);
		// int byteswritten = 0;
		// boolean hasRemaining = true;
		// buf.flip();
		// String content = new String(buf.array()).substring(0,
		// buf.array().length);
		// System.out.println(content);
		// HttpResponse r = new HttpResponse();
		// // r.setBody(new String(buf.array()).substring(0,
		// buf.array().length));
		// // r.setClientaddr(clientaddr);
		for (Filter f : filterList) {
			f.filter(m);
		}
		// byteswritten = receiver.write(buf);
		// hasRemaining = buf.hasRemaining();
		// buf.compact();
		// HttpRequest message = (HttpRequest) parser.parseHeaders(buf);
		// if (!hasRemaining
		// && parser.getState() == ParsingState.Body
		// && byteswritten + incompleteMessage.length()
		// - parser.getHeadersLength() == length) {
		// incompleteMessage = "";
		// return true;
		// }

	}

	public SocketChannel getOppositeChannel(SocketChannel channel) {
		return isClient(channel) ? server : client;
	}

	public boolean isClient(SocketChannel channel) {
		return channel.socket().getPort() == client.socket().getPort()
				&& channel.socket().getInetAddress().getHostName()
						.equals(client.socket().getInetAddress().getHostName());
	}

	public ByteBuffer getBuffer(SocketChannel channel) {
		if (channel.equals(server))
			return serverbuf;
		return clientbuf;
	}

	public SocketChannel getServer() {
		return server;
	}

	public void setServer(SocketChannel server) {
		this.server = server;
		serverport = server.socket().getPort();
		serveraddr = server.socket().getInetAddress().getHostName();
		serverbuf = ByteBuffer.allocate(bufSize);
	}

	public void resetServer() {
		server = null;
		serverport = 0;
		serveraddr = null;
		serverbuf = null;
	}

	public SocketChannel getClient() {
		return client;
	}

	public void setClient(SocketChannel client) {
		this.client = client;
		clientport = client.socket().getPort();
		clientaddr = client.socket().getInetAddress().getHostName();
		clientbuf = ByteBuffer.allocate(bufSize);
	}

	public ByteBuffer getServerbuf() {
		return serverbuf;
	}

	public void setServerbuf(ByteBuffer serverbuf) {
		this.serverbuf = serverbuf;
	}

	public ByteBuffer getClientbuf() {
		return clientbuf;
	}

	public void setClientbuf(ByteBuffer clientbuf) {
		this.clientbuf = clientbuf;
	}
	
	public Message getIncompleteMessage() {
		return this.incompleteMessage;
	}

	public void resetIncompleteMessage() {
		this.incompleteMessage = null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((clientaddr == null) ? 0 : clientaddr.hashCode());
		result = prime * result + clientport;
		result = prime * result
				+ ((serveraddr == null) ? 0 : serveraddr.hashCode());
		result = prime * result + serverport;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProxyConnection other = (ProxyConnection) obj;
		if (clientaddr == null) {
			if (other.clientaddr != null)
				return false;
		} else if (!clientaddr.equals(other.clientaddr))
			return false;
		if (clientport != other.clientport)
			return false;
		if (serveraddr == null) {
			if (other.serveraddr != null)
				return false;
		} else if (!serveraddr.equals(other.serveraddr))
			return false;
		if (serverport != other.serverport)
			return false;
		return true;
	}

}
