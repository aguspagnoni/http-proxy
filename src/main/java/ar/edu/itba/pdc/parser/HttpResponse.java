package ar.edu.itba.pdc.parser;

public class HttpResponse extends Message {

	protected int code; // e.g. 302, 404
	protected String version; // e.g. HTTP/1.1
	protected String verboseCode; // e.g. Moved Temporarily, Bad Request

	// hereda mapa de headers
	// hereda el body

	public boolean isFinished() {
		if (headers.get("content-length") == null)
			return false;
        setContentLength(Integer.valueOf(headers.get("content-length").trim()));
        return getContentLength() == getAmountRead() - getHeadersLength();
	}

	public void fillHead() {
		String[] aux = firstLine.toLowerCase().split(" ");
		if (firstLine != null && aux.length == 3) {
			this.version = aux[0];
			this.code = Integer.parseInt(aux[1]);
			this.verboseCode = aux[2];
		}
	}

	public int getCode() {
		return code;
	}

	public String getVersion() {
		return version;
	}

	public String getVerboseCode() {
		return verboseCode;
	}
}
