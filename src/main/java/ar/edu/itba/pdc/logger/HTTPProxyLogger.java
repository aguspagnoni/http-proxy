package ar.edu.itba.pdc.logger;

import java.util.logging.Logger;

public class HTTPProxyLogger {
	
	private static HTTPProxyLogger instance;
	private Logger logger;
	
	private HTTPProxyLogger()
	{
		//logger = Logger.getLogger(HTTPProxyLogger.class);
		//logger.addApender(new FileAppender(new HTMLLayout(), "logs.html"));
		logger=Logger.getLogger("HTTP Proxy Logger");
		
	}
	
	public synchronized HTTPProxyLogger getInstance(){
		if(instance==null){
			instance=new HTTPProxyLogger();
		}
		return instance;
	}
	
	

}
