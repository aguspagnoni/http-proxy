package ar.edu.itba.pdc.proxy;

import java.nio.channels.SocketChannel;

public class ProxyConnection {

	private SocketChannel server;
	private SocketChannel client;

	private int serverport;
	private int clientport;
	private String serveraddr;
	private String clientaddr;

	public ProxyConnection() {

	}

	public SocketChannel getServer() {
		return server;
	}

	public void setServer(SocketChannel server) {
		this.server = server;
		serverport = server.socket().getPort();
		serveraddr = server.socket().getInetAddress().getHostName();
	}

	public SocketChannel getClient() {
		return client;
	}

	public void setClient(SocketChannel client) {
		this.client = client;
		clientport = client.socket().getPort();
		clientaddr = client.socket().getInetAddress().getHostName();
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
