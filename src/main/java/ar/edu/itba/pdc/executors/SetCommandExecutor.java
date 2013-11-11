package ar.edu.itba.pdc.executors;

import ar.edu.itba.pdc.configuration.ConfigurationCommands;
import ar.edu.itba.pdc.parser.PDCResponse;

public class SetCommandExecutor extends AbstractCommandExecutor {

	private static SetCommandExecutor instance = null;
	private ConfigurationCommands commandManager;
	
	public static SetCommandExecutor getInstance() {
		if (instance == null)
			instance = new SetCommandExecutor();
		return instance;
	}
	
	private SetCommandExecutor() {
		commandManager = ConfigurationCommands.getInstance();
	}
	
	public PDCResponse execute(String command, String value) {
		String commandLower = command.toLowerCase();
		if (commandLower.equals("interval")) {
			try {
				if (Integer.valueOf(value) < 10000)
					return null;
			} catch (NumberFormatException e) {
				return null;
			}
			this.commandManager.setProperty(commandLower, value);
			//getLogger().info("Changed interval to " + value);
			return null;
			//return "OK";
		}
		return null;
					
	}

}
