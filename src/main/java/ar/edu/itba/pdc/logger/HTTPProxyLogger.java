package ar.edu.itba.pdc.logger;

import java.io.IOException;
import java.util.logging.Logger;

public class HTTPProxyLogger {
	
	private static HTTPProxyLogger instance;
	private Logger logger;
	
	private HTTPProxyLogger()
	{
		//logger = Logger.getLogger(HTTPProxyLogger.class);
		logger=Logger.getLogger("FILE");

//		logger.addApender(new FileAppender(new SimpleLayout(), "logs"));
		
	}
	
	public static HTTPProxyLogger getInstance() throws IOException{
		if(instance==null)
			instance=new HTTPProxyLogger();

	 return instance;
	}
	
	public void info(String message){
		logger.info(message);
	}
	
	
	
	
	public static void main(String[] args) throws IOException {
		HTTPProxyLogger.getInstance();
	}
	

}
