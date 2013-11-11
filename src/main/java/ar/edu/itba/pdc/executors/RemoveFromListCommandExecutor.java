package ar.edu.itba.pdc.executors;

import ar.edu.itba.pdc.configuration.ConfigurationCommands;
import ar.edu.itba.pdc.parser.PDCResponse;

public class RemoveFromListCommandExecutor extends AbstractCommandExecutor {

	private static RemoveFromListCommandExecutor instance = null;
	private ConfigurationCommands commandManager;
	
	public static RemoveFromListCommandExecutor getInstance() {
		if (instance == null)
			instance = new RemoveFromListCommandExecutor();		
		return instance;
	}
	
	private RemoveFromListCommandExecutor() {
		commandManager = ConfigurationCommands.getInstance();
	}
	
	public PDCResponse execute(String command, String value) {	
		String newValue = "";
		String oldValue = "";
		
		if (commandManager.hasProperty("silenceuser"))
			oldValue = commandManager.getProperty("silenceuser");
		
		if (oldValue != "" && oldValue != null && !oldValue.contains(value)) {
			return null;
		}

		String[] oldValuesArray = oldValue.split(";");
		
		for (String auxVal : oldValuesArray) {
			if (!auxVal.equals(value)) {
				newValue += ";" + auxVal;
			}
		}
		
		commandManager.setProperty("silenceuser", newValue.substring(1));
	//	getLogger().info("Removed " + value + " from " + command + " list");
		return null;
		//return "OK";
	}
	
}
