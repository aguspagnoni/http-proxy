package ar.edu.itba.pdc.executors;

import ar.edu.itba.pdc.logger.HTTPProxyLogger;


public abstract class AbstractCommandExecutor implements CommandExecutor { 

	//private HTTPProxyLogger logger = HTTPProxyLogger.getInstance();

	protected HTTPProxyLogger getLogger() {
		return null;
		//return logger;
	}
	
}
