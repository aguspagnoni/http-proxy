package ar.edu.itba.pdc.parser;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import ar.edu.itba.pdc.configuration.ConfigurationCommands;
import ar.edu.itba.pdc.exceptions.BadSyntaxException;
import ar.edu.itba.pdc.executors.AuthService;
import ar.edu.itba.pdc.executors.BooleanCommandExecutor;
import ar.edu.itba.pdc.executors.CommandExecutor;
import ar.edu.itba.pdc.executors.GetCommandExecutor;
import ar.edu.itba.pdc.executors.RemoveFromListCommandExecutor;
import ar.edu.itba.pdc.executors.ValueCommandExecutor;

public class StupidAdminParser {
	private Map<String, CommandExecutor> commandTypes = new HashMap<String, CommandExecutor>();
	private ConfigurationCommands commandManager;

	public StupidAdminParser() {
		commandManager = ConfigurationCommands.getInstance();
		commandTypes.put("statistics", BooleanCommandExecutor.getInstance());
		commandTypes.put("getStatistics", GetCommandExecutor.getInstance());
		commandTypes
				.put("transformation", BooleanCommandExecutor.getInstance());
		RemoveFromListCommandExecutor.getInstance();
		commandTypes.put("auth", AuthService.getInstance());
		commandTypes.put("interval", ValueCommandExecutor.getInstance());
		commandTypes.put("byteUnit", ValueCommandExecutor.getInstance());
	}

	public String parseCommand(ByteBuffer readBuffer, int bytesRead)
			throws BadSyntaxException {

		String fullCommand = new String(readBuffer.array()).substring(0,
				bytesRead);
		Map<String, String> commands = new HashMap<String, String>();
		for (String s : fullCommand.split(";")) {

			String[] aux = s.split("=");
			String trimmed = aux[0].trim();
			if (commandTypes.containsKey(trimmed)) {
				if (aux.length > 1) {
					commands.put(trimmed, aux[1].trim());
				} else {
					commands.put(trimmed, "");
				}
			} else if (trimmed.isEmpty()) {
				readBuffer.rewind();
				return null;
			} else {
				readBuffer.rewind();
				throw new BadSyntaxException();
			}
		}
		readBuffer.rewind();
		return takeActions(commands);
	}

	/**
	 * Once the commands were parsed, takes the appropriate action using the
	 * executors stored in the commandTypes map.
	 * 
	 * @param commands
	 * @return
	 * @throws BadSyntaxException
	 */

	private String takeActions(Map<String, String> commands)
			throws BadSyntaxException {

		String responseToAdmin = null;
		for (String cmd : commands.keySet()) {
			responseToAdmin = commandTypes.get(cmd).execute(cmd,
					commands.get(cmd));

			if (responseToAdmin != null) {
				commandManager.saveFile();
			} else {
				throw new BadSyntaxException();
			}
		}
		return responseToAdmin + '\n';
	}
}
