package ar.edu.itba.pdc.logger;

import java.io.IOException;
import java.util.logging.Logger;

public class HTTPProxyLogger {
	
	private static HTTPProxyLogger instance;
	private Logger logger;
	
	private HTTPProxyLogger()
	{
		//logger = Logger.getLogger(HTTPProxyLogger.class);
		logger=Logger.getLogger("HTTP Proxy Logger");
//		logger.addApender(new FileAppender(new SimpleLayout(), "logs"));
		
	}
	
	public static HTTPProxyLogger getInstance() throws IOException{
		if(instance==null)
			instance=new HTTPProxyLogger();

	 return instance;
	}
	
	

}
