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
		req=new PDCRequest();
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
	 * Sets one of the two buffers given the BufferType
	 * 
	 * @param type
	 * @param buffer
	 */

	public void setBuffer(BufferType type, ByteBuffer buffer) {
		buffers.put(type, buffer);
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
	 * Writes the given byte array in one of the buffers depending on which
	 * BufferType (read or write) is received by parameter.
	 * 
	 * @param type
	 * @param bytes
	 */

	public void writeToBuffer(BufferType type, byte[] bytes) {
		ByteBuffer buf = (type == BufferType.read) ? buffers
				.get(BufferType.read) : buffers.get(BufferType.write);
		if (buf.capacity() - buf.remaining() < bytes.length) {
			buf.flip();
			buf = ByteBuffer.allocate(buf.capacity() + bytes.length).put(buf);
			if (type == BufferType.read) {
				setBuffer(BufferType.read, buf);
			} else {
				setBuffer(BufferType.write, buf);
			}
		}
		buf.put(bytes);
	}

	/**
	 * Clears the given buffer (read or write)
	 * 
	 * @param type
	 */

	public void clearBuffer(BufferType type) {
		if (buffers.get(type) != null)
			buffers.get(type).clear();
	}

	/**
	 * Compacts the given buffer (read or write)
	 * 
	 * @param type
	 */

	public void compactBuffer(BufferType type) {
		if (buffers.get(type) != null)
			buffers.get(type).compact();
	}

	/**
	 * Flips the given buffer (read or write)
	 * 
	 * @param type
	 */

	public void flipBuffer(BufferType type) {
		if (buffers.get(type) != null)
			buffers.get(type).flip();
	}

	/**
	 * Returns the buffer array of the given buffer (read or write)
	 * 
	 * @param type
	 */

	public byte[] getBufferArray(BufferType type) {
		if (buffers.get(type) != null)
			return buffers.get(type).array();
		return null;
	}

	/**
	 * Expands the given buffer (read or write)
	 * 
	 * @param type
	 */

	public void expandBuffer(BufferType type) {
		ByteBuffer buf = getBuffer(type);
		buf.flip();
		setBuffer(type, ByteBuffer.allocate(buf.capacity() * 2).put(buf));
	}

	/**
	 * Returns true if the buffer has remaining information
	 * 
	 * @param type
	 */

	public boolean hasRemainingFor(BufferType type) {
		return buffers.get(type) != null && buffers.get(type).hasRemaining();
	}

	/**
	 * Returns true if the buffer has remaining information
	 * 
	 * @param type
	 */

	public boolean hasInformationFor(BufferType type) {
		return buffers.get(type) != null
				&& (buffers.get(type).capacity() != buffers.get(type)
						.remaining());
	}
	
	/**
	 * Returns the PDCRequest asociated to that channelBuffers
	 * @return
	 */
	public PDCRequest getRequest(){
		return this.req;
	}
}
