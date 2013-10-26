package ar.edu.itba.pdc.executors;

import ar.edu.itba.pdc.configuration.ConfigurationCommands;

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
	
	public String execute(String command, String value) {	

		Integer newValue;
		
		if ((newValue = Integer.parseInt(value)) == null)
			return null;
		else {
			commandManager.setProperty(command, value);
			if (command.equals("interval"))
				System.out.println();//REMOVE
				//StatisticsFilter.getInstance().setInterval(newValue);
			if (command.equals("byteUnit"))
				System.err.println(); //REMOVE
				//StatisticsFilter.getInstance().setByteUnit(newValue);
		}
		return "OK";
	}

}
