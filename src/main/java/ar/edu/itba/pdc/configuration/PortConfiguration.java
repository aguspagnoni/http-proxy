package ar.edu.itba.pdc.configuration;

import java.io.IOException;
import java.util.Properties;

public class PortConfiguration {
	private final String CONFIG_FILE_NAME="ar/edu/itba/pdc/configuration/portconfiguration.properties";
	private Properties properties=null;
	
	public PortConfiguration(){
		this.properties = new Properties();
        try {
            properties.load(PortConfiguration.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public int getPortClient(){
		return Integer.parseInt(getAtribute("portclient"));
	}
	public int getPortAdmin(){
		return Integer.parseInt(getAtribute("portadmin"));
	}
	
	private String getAtribute(String atr){
		return properties.getProperty(atr);
	}
}
