package ar.edu.itba.pdc.proxy;

import java.io.IOException;

public class HTTPProxy {
	public static void main(String[] args) {
		HttpServerSelector httpsrv = new HttpServerSelector();
		try {
			httpsrv.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}