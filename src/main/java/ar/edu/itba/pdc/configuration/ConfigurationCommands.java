package ar.edu.itba.pdc.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigurationCommands {
	private Properties props;
	private FileInputStream fis;

	private static ConfigurationCommands instance;

	public static ConfigurationCommands getInstance() {
		if (instance == null)
			instance = new ConfigurationCommands();
		return instance;
	}

	/**
	 * Loads all the properties from the file
	 * resources/parsedcommands.properties
	 */

	private ConfigurationCommands() {
		this.props = new Properties();
		try {
			String current = new java.io.File(".").getCanonicalPath();
			this.fis = new FileInputStream(
					current
							+ "/src/main/java/ar/edu/itba/pdc/resources/parsedcommands.properties");
			props.load(fis);

		} catch (Exception e) {
		}
	}

	/**
	 * Returns the value of a given property or null if it is not in the
	 * properties file.
	 * 
	 * @param property
	 * @return
	 */

	public String getProperty(String property) {
		if (props.get(property) == null)
			return "";
		return props.get(property).toString();
	}

	/**
	 * Sets the value of a given property.
	 * 
	 * @param property
	 * @param value
	 */

	public void setProperty(String property, String value) {
		props.setProperty(property, value);
	}

	/**
	 * Returns true if the file resources/parsedcommands.properties contains the
	 * given property
	 * 
	 * @param property
	 * @return
	 */

	public boolean hasProperty(String property) {
		return props.containsKey(property);
	}

	/**
	 * Commits the local changes back to the properties file.
	 */

	public void saveFile() {
		String current = "";
		FileOutputStream ops = null;
		try {
			current = new java.io.File(".").getCanonicalPath();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			ops = new FileOutputStream(
					current
							+ "/src/main/java/ar/edu/itba/pdc/resources/parsedcommands.properties",
					false);
		} catch (FileNotFoundException e) {
		}
		try {
			props.store(ops, "Commands");
		} catch (IOException e) {
		}
	}

}
