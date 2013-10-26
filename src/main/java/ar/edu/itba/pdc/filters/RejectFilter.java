package ar.edu.itba.pdc.filters;

import ar.edu.itba.pdc.parser.Message;

public class RejectFilter implements Filter {
	String URL;

	public boolean filter(Message m) {
		if (this.URL.equals(m.getBody())) {
			return false;
		}
		return true;
	}

}
