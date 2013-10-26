package ar.edu.itba.pdc.executors;

import ar.edu.itba.pdc.configuration.ConfigurationCommands;

public class AddToListCommandExecutor extends AbstractCommandExecutor {

	private static AddToListCommandExecutor instance = null;
	private ConfigurationCommands commandManager;
	
	public static AddToListCommandExecutor getInstance() {
		if (instance == null)
			instance = new AddToListCommandExecutor();		
		return instance;
	}
	
	private AddToListCommandExecutor() {
		commandManager = ConfigurationCommands.getInstance();
	}
	
	public String execute(String command, String value) {	
		String oldValue = "";
		if (commandManager.hasProperty(command))
			oldValue = commandManager.getProperty(command);
		String newValue = value;
		
		if (oldValue != null && oldValue.contains(newValue)) {
			return null;
		}

		if (oldValue != null && !oldValue.equals(""))
			commandManager.setProperty(command, oldValue + ";" + newValue);
		else
			commandManager.setProperty(command, newValue);
		
		//getLogger().info("Added " + value + " to " + command + " list");
		return "OK";
	}

}
