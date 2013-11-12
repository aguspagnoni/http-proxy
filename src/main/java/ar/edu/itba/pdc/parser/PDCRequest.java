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

public class PDCRequest extends Message {

	private String operation;
	private String param;
	private String version;
	private Map<String, CommandExecutor> commandTypes = new HashMap<String, CommandExecutor>();
	private ConfigurationCommands commandManager;
	private boolean logged = false;

	public PDCRequest() {

		commandManager = ConfigurationCommands.getInstance();
		// commandTypes.put("statistics", BooleanCommandExecutor.getInstance());
		commandTypes.put("gethistogram", GetCommandExecutor.getInstance());
		commandTypes.put("getaccesses", GetCommandExecutor.getInstance());
		commandTypes.put("gettxbytes", GetCommandExecutor.getInstance());
		// commandTypes.put("transformation", );
		// RemoveFromListCommandExecutor.getInstance();
		commandTypes.put("authentication", AuthService.getInstance());

		// commandTypes.put("interval", ValueCommandExecutor.getInstance());
		// commandTypes.put("byteUnit", ValueCommandExecutor.getInstance());

		commandTypes.put("filter", BooleanCommandExecutor.getInstance());
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

	public PDCResponse parseMessage(ByteBuffer readBuffer, int bytesRead) {

		if (!version.equals("pdc/1.0")) {
			return new PDCResponse(404, "pdc/1.0", "Wrong PDC Version");
			// aca vendria error 404 NOT FOUND
		}

		if (operation != null && operation.equals("get")) {
			if (param != null) {
				if (!commandTypes.containsKey(operation + param)) {
					return new PDCResponse(404, "PDC/1.0");
					// aca vendria error 404 NOT FOUND
				}
				if (bytesRead != 0) {
					return new PDCResponse(420, "PDC/1.0", "Corrupted Data"); // la
																				// DATA
																				// en
																				// get
					// tiene que estar
					// vacia
					// aca vendria error 420 CORRUPTED DATA
				}

			} else {
				return new PDCResponse(400, "PDC/1.0", "BAD Request");
				// 400 BAD REQUEST
			}

		} else if (operation != null && (operation.equals("filter"))) {
			if (!commandTypes.containsKey(operation)) {
				return new PDCResponse(404, "PDC/1.0", "Not found filter");
				// aca vendria error 404 NOT FOUND
			}
			// if (bytesRead == 0) {
			// return new PDCResponse(420, "PDC/1.0", "Corrupted data"); // la
			// DATA no puede
			// // estar vacia
			// // aca vendria error 420 CORRUPTED DATA
			// }

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
		String user;
		String password;
		if (operation.equals("get")) {
			if (!logged)
				return new PDCResponse(401, "PDC/1.0", "Not logged");

			responseToAdmin = commandTypes.get(operation + param).execute(
					operation, param);
		} else {
			if (operation.equals("authenticate")) {
				String params[] = param.split(":");
				if (params.length == 2) {
					user = params[0].trim();
					password = params[1].trim();
				} else {
					return new PDCResponse(400, "PDC/1.0", "Wrong Syntax");
				}
				// String auth = headers.get("authentication");
				responseToAdmin = commandTypes.get("authentication").execute(
						user, password); // no estamos
											// contemplando el
				// usuario,
				// habría
				// que ver de cambiarlo en el executor o
				// ver
				// cómo hacemos
				if (responseToAdmin != null)
					logged = true;
				else
					return new PDCResponse(200, "PDC/1.0", "Wrong password");
			} else if (operation.equals("filter")) {
				responseToAdmin = commandTypes.get("filter").execute(operation,
						param);
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
