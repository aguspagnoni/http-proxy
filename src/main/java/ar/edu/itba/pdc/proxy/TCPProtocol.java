package ar.edu.itba.pdc.proxy;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface TCPProtocol {
      void handleAccept(SocketChannel newChannel) throws IOException;
      SocketChannel handleRead(SelectionKey key) throws IOException;
      void handleWrite(SelectionKey key) throws IOException;
}