package ar.edu.itba.pdc.executors;

import ar.edu.itba.pdc.configuration.ConfigurationCommands;
import ar.edu.itba.pdc.filters.StatisticsFilter;

public class GetCommandExecutor extends AbstractCommandExecutor {

	private static GetCommandExecutor instance = null;
	private ConfigurationCommands commandManager;

	public static GetCommandExecutor getInstance() {
		if (instance == null)
			instance = new GetCommandExecutor();
		return instance;
	}

	private GetCommandExecutor() {
		commandManager = ConfigurationCommands.getInstance();
	}

	public String execute(String command, String value) {
		commandManager.saveFile();
		String ans = null;
		if (command.equals("getStatistics")) {
			// getLogger().info("Statistics answered to administrator");
			ans = StatisticsFilter.getInstance().execute();
		} // else if (command.equals("monitor")) {
			// ans = StatisticsFilter.getInstance().executeLatest();
			// }
		return ans;
	}
}
