package ar.edu.itba.pdc.proxy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

import ar.edu.itba.pdc.filters.Filter;
import ar.edu.itba.pdc.filters.StatisticsFilter;
import ar.edu.itba.pdc.filters.TransformationFilter;
import ar.edu.itba.pdc.parser.HttpParser;
import ar.edu.itba.pdc.parser.HttpRequest;
import ar.edu.itba.pdc.parser.HttpResponse;
import ar.edu.itba.pdc.parser.Message;

public class ProxyConnection {

	private int bufSize = 500000; // Size of I/O buffer
	private SocketChannel server;
	private SocketChannel client;

	private int serverport;
	private int clientport;
	private String serveraddr;
	private String clientaddr;

	private ByteBuffer serverbuf;
	private ByteBuffer clientbuf;

	private String incompleteMessage = "";

	private HttpParser parser = new HttpParser();
	private List<Filter> filterList;

	public ProxyConnection() {
		initialize();
	}

	private void initialize() {
		filterList.add(StatisticsFilter.getInstance());
		filterList.add(TransformationFilter.getInstance());
	}

	public Message getMessage(SocketChannel sender) {
		ByteBuffer buf = getBuffer(sender);
		HttpRequest message = (HttpRequest) parser.parseHeaders(buf);
		if (parser.hasFinished())
			return message;
		return null;
	}

	public boolean handleWrite(SocketChannel sender) throws IOException {
		SocketChannel receiver = getOppositeChannel(sender);
		ByteBuffer buf = getBuffer(sender);
		int byteswritten = 0;
		boolean hasRemaining = true;
		buf.flip(); // Prepare buffer for writing
		String content = new String(buf.array()).substring(0,
				buf.array().length);
		System.out.println(content);
		HttpResponse r = new HttpResponse();
		r.setBody(new String(buf.array()).substring(0, buf.array().length));
		r.setClientaddr(clientaddr);
		for (Filter f : filterList) {
			f.filter(r);
		}
		byteswritten = receiver.write(buf);
		hasRemaining = buf.hasRemaining(); // Buffer completely written?
		buf.compact(); // Make room for more data to be read in
		// HttpRequest message = (HttpRequest) parser.parseHeaders(buf);
		// if (!hasRemaining && parser.getState() == ParsingState.Body
		// && byteswritten + incompleteMessage.length() -
		// parser.getHeadersLength()
		// == length) {
		// incompleteMessage = ""; // reset the incomplete message
		// return true; // finished writting httpmessage is complete
		// }
		//
		// }
		// return false;
		return true;
	}

	public SocketChannel getOppositeChannel(SocketChannel channel) {
		boolean isserver = channel.socket().getPort() == server.socket()
				.getPort()
				&& channel.socket().getInetAddress().getHostName()
						.equals(server.socket().getInetAddress().getHostName());
		return isserver ? client : server;
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
