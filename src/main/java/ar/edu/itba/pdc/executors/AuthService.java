package ar.edu.itba.pdc.executors;

import org.apache.commons.codec.binary.Base64;

import ar.edu.itba.pdc.configuration.ConfigurationCommands;
import ar.edu.itba.pdc.parser.PDCResponse;

/**
 * Executes the authentication service ("authorization" header in PDC protocol)
 * @author grupo 3
 *
 */
public class AuthService extends AbstractCommandExecutor {

	private static AuthService instance = null;
	private String password = "12345";
	private String username = "user";
	private ConfigurationCommands commandManager;

	public static AuthService getInstance() {
		if (instance == null)
			instance = new AuthService();
		return instance;
	}

	private AuthService() {
		this.commandManager = ConfigurationCommands.getInstance();
		if (commandManager.hasProperty("password"))
			this.password = new String(Base64.decodeBase64(commandManager
					.getProperty("password").getBytes()));
		if (commandManager.hasProperty("username"))
			this.username = new String(commandManager.getProperty("username")
					.getBytes());
	}

	/**
	 * Validates if username and password are correct.
	 */
	public PDCResponse execute(String username, String password) {
		username = "user"; // Future Extension
		return (checkAuth(username, password)) ? null : new PDCResponse(200,
				"PDC/1.0", "Wrong Auth ");
	}

	private boolean checkAuth(String username, String password) {
		if (username.equals(this.username) && password.equals(this.password)) {
			getLogger().info("[Admin Handler] Administrator logged in");
			return true;
		}
		getLogger()
				.info("[Admin Handler] Administrator authorization rejected");
		return false;
	}

}
