package ar.edu.itba.pdc.parser;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import ar.edu.itba.pdc.parser.enumerations.ParsingState;

public abstract class Message {

	protected Map<String, String> headers = new HashMap<String, String>();
	protected int contentLength;
	protected int headersLength;
	protected int amountRead;

	protected String firstLine = "";
	protected ParsingState state = ParsingState.Head;
	protected String from;
	protected ByteBuffer partialHeaders = null;

	protected void addHeader(String line) {
		String[] kv = line.split(":");
		if (kv.length > 1)
			headers.put(kv[0].trim().toLowerCase(), kv[1].trim().toLowerCase());
	}

	public abstract boolean isFinished();

	/*
	 * Implementations should make the first line of a message useful.
	 * 
	 * e.g. HTTP request's head: GET /resource HTTP/1.1
	 * 
	 * This method should recognize the method, resource path and the protocol
	 * version.
	 */
	public abstract void fillHead();

	public int getContentLength() {
		return contentLength;
	}

	public int getHeadersLength() {
		return headersLength;
	}

	public void increaseHeadersLength(int headersLength) {
		this.headersLength += headersLength;
	}

	public void setContentLength(int length) {
		this.contentLength = length;
	}

	public int getAmountRead() {
		return amountRead;
	}

	public void increaseAmountRead(int bytesRead) {
		this.amountRead += bytesRead;
	}

	public String getFrom() {
		return this.from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public ParsingState getState() {
		return this.state;
	}

	public void saveInBuffer(ByteBuffer buf) {
		if (this.partialHeaders == null)
			this.partialHeaders = ByteBuffer.allocate(8192);
		this.partialHeaders.put(buf);
	}

	public ByteBuffer getPartialHeadersBuffer() {
		return this.partialHeaders;
	}

	public boolean hasHeadersBuffered() {
		return this.partialHeaders != null;
	}

	public void finishWithLeftHeaders() {
		this.partialHeaders = null;
	}

	public void setState(ParsingState p) {
		this.state = p;
	}

}
