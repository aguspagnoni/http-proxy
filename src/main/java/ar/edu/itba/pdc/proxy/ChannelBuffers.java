package ar.edu.itba.pdc.proxy;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import ar.edu.itba.pdc.parser.PDCRequest;

public class ChannelBuffers {
	private static final int BUFFER_SIZE = 4096;

	private Map<BufferType, ByteBuffer> buffers;
	private PDCRequest req;

	public ChannelBuffers() {
		initializeMap(ByteBuffer.allocate(BUFFER_SIZE),
				ByteBuffer.allocate(BUFFER_SIZE));
		req = new PDCRequest();
	}

	public ChannelBuffers(ByteBuffer readBuffer, ByteBuffer writeBuffer) {
		initializeMap(readBuffer, writeBuffer);
	}

	/**
	 * Puts both read and write buffers into a map with their specific
	 * BufferTypes
	 * 
	 * @param readBuffer
	 * @param writeBuffer
	 */

	private void initializeMap(ByteBuffer readBuffer, ByteBuffer writeBuffer) {
		buffers = new HashMap<BufferType, ByteBuffer>();
		buffers.put(BufferType.read, readBuffer);
		buffers.put(BufferType.write, writeBuffer);
	}

	/**
	 * Returns one of the two buffers given the BufferType
	 * 
	 * @param type
	 */

	public ByteBuffer getBuffer(BufferType type) {
		return buffers.get(type);
	}

	/**
	 * Returns the PDCRequest asociated to that channelBuffers
	 * 
	 * @return
	 */
	public PDCRequest getRequest() {
		return this.req;
	}
}
