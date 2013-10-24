package ar.edu.itba.pdc.configuration;

import java.io.IOException;
import java.util.Properties;

public class Configuration {
	private final String CONFIG_FILE_NAME="ar/edu/itba/pdc/configuration/configuration.properties";
	private Properties properties=null;
	
	public Configuration(){
		this.properties = new Properties();
        try {
            properties.load(Configuration.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public int getPort(){
		return Integer.parseInt(getAtribute("port"));
	}
	
	private String getAtribute(String atr){
		return properties.getProperty(atr);
	}
}
