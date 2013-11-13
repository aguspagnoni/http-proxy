package ar.edu.itba.pdc.parser;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import ar.edu.itba.pdc.configuration.ConfigurationCommands;
import ar.edu.itba.pdc.exceptions.BadSyntaxException;
import ar.edu.itba.pdc.executors.AuthService;
import ar.edu.itba.pdc.executors.BooleanCommandExecutor;
import ar.edu.itba.pdc.executors.CommandExecutor;
import ar.edu.itba.pdc.executors.GetCommandExecutor;
import ar.edu.itba.pdc.executors.RemoveFromListCommandExecutor;
import ar.edu.itba.pdc.executors.ValueCommandExecutor;
import ar.edu.itba.pdc.parser.enumerations.ParsingState;

public class PDCRequest extends Message {

	private String operation;
	private String param;
	private String version;
	private Map<String, CommandExecutor> commandTypes = new HashMap<String, CommandExecutor>();
	private ConfigurationCommands commandManager;
	private boolean logged = false;

	public PDCRequest() {

		commandManager = ConfigurationCommands.getInstance();
		commandTypes.put("disablestatistics",
				BooleanCommandExecutor.getInstance());
		commandTypes.put("enablestatistics",
				BooleanCommandExecutor.getInstance());
		commandTypes.put("gethistogram", GetCommandExecutor.getInstance());
		commandTypes.put("getaccesses", GetCommandExecutor.getInstance());
		commandTypes.put("gettxbytes", GetCommandExecutor.getInstance());
		// commandTypes.put("transformation", );
		// RemoveFromListCommandExecutor.getInstance();
		commandTypes.put("authorization", AuthService.getInstance());

		// commandTypes.put("interval", ValueCommandExecutor.getInstance());
		// commandTypes.put("byteUnit", ValueCommandExecutor.getInstance());

		commandTypes.put("enablefilter", BooleanCommandExecutor.getInstance());
		commandTypes.put("disablefilter", BooleanCommandExecutor.getInstance());

	}

	@Override
	public boolean isFinished() {
		if (operation != null && param != null && version != null) {
			return true;
		}
		return false;
	}

	@Override
	public void fillHead() {
		String[] aux = firstLine.split(" ");
		if (firstLine != null && aux.length == 3) {
			this.operation = aux[0].toLowerCase().trim();
			this.param = aux[1].toLowerCase().trim();
			this.version = aux[2].toLowerCase().trim();
		}
	}

	public PDCResponse parseMessage() {
		
		if (version == null || operation == null || param == null)
			return new PDCResponse(404, "pdc/1.0",
					"Bad Request. Try [Method] [OPERATION] [VERSION]");
		if (!version.equals("pdc/1.0")) {
			return new PDCResponse(404, "pdc/1.0", "Wrong PDC Version");
		}
		if (!headers.containsKey("authorization")) {
			return new PDCResponse(401, "PDC/1.0", "Unauthorized");
		}

		return takeActions();
	}

	/**
	 * Once the commands were parsed, takes the appropriate action using the
	 * executors stored in the commandTypes map.
	 * 
	 * @param commands
	 * @return
	 * @throws BadSyntaxException
	 */

	private PDCResponse takeActions() throws BadSyntaxException {
		PDCResponse responseToAdmin = null;
		String password;
		password = headers.get("authorization");
		responseToAdmin = commandTypes.get("authorization").execute("user",
				password);
		if (responseToAdmin != null) {
			return responseToAdmin;
		}
		if (operation.equals("get")) {
			responseToAdmin = commandTypes.get(operation + param).execute(
					operation, param);
		} else {
			if (operation.equals("filter")) {
				responseToAdmin = commandTypes.get("filter").execute(operation,
						param);
			} else {
				CommandExecutor c = commandTypes.get(operation + param);
				if (c != null)
					responseToAdmin = c.execute(operation, param);
			}
		}
		if (responseToAdmin != null) {
			commandManager.saveFile();
		} else {
			throw new BadSyntaxException();
		}
		return responseToAdmin;
	}

}
