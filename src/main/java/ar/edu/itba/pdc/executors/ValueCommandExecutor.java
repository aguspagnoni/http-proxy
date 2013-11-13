package ar.edu.itba.pdc.executors;

import ar.edu.itba.pdc.configuration.ConfigurationCommands;
import ar.edu.itba.pdc.parser.PDCResponse;

public class ValueCommandExecutor extends AbstractCommandExecutor {

	private static ValueCommandExecutor instance = null;
	private ConfigurationCommands commandManager;

	public static ValueCommandExecutor getInstance() {
		if (instance == null)
			instance = new ValueCommandExecutor();
		return instance;
	}

	private ValueCommandExecutor() {
		commandManager = ConfigurationCommands.getInstance();
	}

	public PDCResponse execute(String command, String value) {

		@SuppressWarnings("unused")
		Integer newValue;

		if ((newValue = Integer.parseInt(value)) == null)
			return null;
		else {
			commandManager.setProperty(command, value);
		}
		return null;
		// return "OK";
	}

}
