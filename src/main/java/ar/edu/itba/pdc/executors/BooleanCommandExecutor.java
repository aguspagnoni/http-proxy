package ar.edu.itba.pdc.executors;

import ar.edu.itba.pdc.configuration.ConfigurationCommands;
import ar.edu.itba.pdc.parser.PDCResponse;

public class BooleanCommandExecutor extends AbstractCommandExecutor {

	private static BooleanCommandExecutor instance = null;
	private ConfigurationCommands commandManager;

	public static BooleanCommandExecutor getInstance() {
		if (instance == null)
			instance = new BooleanCommandExecutor();
		return instance;
	}

	private BooleanCommandExecutor() {
		commandManager = ConfigurationCommands.getInstance();
	}

	public PDCResponse execute(String command, String value) {
		String commandLower = command.toLowerCase();
		if (!commandLower.equals("enable") && !commandLower.equals("disable")) {
			return null;
		}
		getLogger().info(
				"[Admin Handler]Set property " + value + " with value "
						+ command + "d");
		if (commandLower.equals("enable"))
			commandManager.setProperty(value, "on");
		else
			commandManager.setProperty(value, "off");
		return new PDCResponse(200, "PDC/1.0", value + " " + commandLower + "d");
	}
}