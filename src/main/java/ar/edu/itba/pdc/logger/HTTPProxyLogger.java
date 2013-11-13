package ar.edu.itba.pdc.logger;

import java.io.IOException;

import org.apache.log4j.Logger;


/**
 * Logs message into a file configured src/main/resources/log4j.properties
 * @author grupo 3
 *
 */
public class HTTPProxyLogger {
	
	private static HTTPProxyLogger instance;
	private Logger logger;
	
	private HTTPProxyLogger()
	{
		//logger = Logger.getLogger(HTTPProxyLogger.class);
		logger=Logger.getLogger("FILE");
		
//		logger.addApender(new FileAppender(new SimpleLayout(), "logs"));
		
	}
	
	public static HTTPProxyLogger getInstance() {
		if(instance==null)
			instance=new HTTPProxyLogger();
 
	 return instance;
	}
	
	/**
	 * Logs a message into file
	 * @param message
	 */
	public void info(String message){
		logger.info(message);
	}
	
	

}
