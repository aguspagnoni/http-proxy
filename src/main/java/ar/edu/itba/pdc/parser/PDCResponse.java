package ar.edu.itba.pdc.parser;

import java.util.HashMap;
import java.util.Map;

public class PDCResponse extends Message {

	protected Map<Integer, String> codeMapping = new HashMap<Integer, String>();
	protected int code; // e.g. 400, 404
	protected String version; // e.g. PDC/1.0
	protected String verboseCode; // e.g. Moved Temporarily, Bad Request
	protected String data;
	protected String body;

	public PDCResponse() {
		codeMapping.put(200, "OK");
		codeMapping.put(204, "NO CONTENT");
		codeMapping.put(400, "BAD REQUEST");
		codeMapping.put(401, "UNAUTHORIZED");
		codeMapping.put(404, "NOT FOUND");
		codeMapping.put(408, "REQUEST TIMEOUT");
		codeMapping.put(420, "CORRUPTED DATA");
		codeMapping.put(500, "INTERNAL SERVER ERROR");
		codeMapping.put(501, "NOT IMPLEMENTED");
		codeMapping.put(503, "SERVICE UNAVAILABLE");
	}

	public PDCResponse(int code, String version) {
		this();
		this.code = code;
		this.version = version;
		this.verboseCode = codeMapping.get(code);
		this.body = "";
	}

	public PDCResponse(int code, String version, String body) {
		this(code, version);
		this.body = body;
	}

	public String getVerboseCode() {
		return this.verboseCode;
	}

	public int getCode() {
		return this.code;
	}

	public String getVersion() {
		return this.version;
	}

	public String getData() {
		if (this.data == null)
			return "\n";
		return this.data;
	}

	public void appendData(String data) {
		if (this.data == null) {
			this.data = data;
		} else {
			this.data = this.data + '\n' + data;
		}
	}

	@Override
	public boolean isFinished() {
		return false;
	}

	@Override
	public void fillHead() {
		return;

	}

	public byte[] getBytes() {
		return this.body.getBytes();
	}

	public String getBody() {
		return this.body;
	}

}
