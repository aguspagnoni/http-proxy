package ar.edu.itba.pdc.executors;

import org.apache.commons.codec.binary.Base64;

import ar.edu.itba.pdc.configuration.ConfigurationCommands;


public class AuthService extends AbstractCommandExecutor {

	private static AuthService instance = null;
	private String password = "secreto546";
	private ConfigurationCommands commandManager;
	
	public static AuthService getInstance() {
		if (instance == null)
			instance = new AuthService();
		return instance;
	}
	
	private AuthService() { 
		this.commandManager = ConfigurationCommands.getInstance();
		if (commandManager.hasProperty("password"))
			this.password = new String(Base64.decodeBase64(commandManager.getProperty("password").getBytes()));
	}
	
	public String execute(String command, String value) {
		if (command.equals("auth"))
			return checkAuth(value);
		else if (command.equals("changePassword"))
			return passwordChange(value);
		return null;
	}
	
	private String passwordChange(String newPassword) {
		this.password = newPassword;
		this.commandManager.setProperty("password", new String(Base64.encodeBase64(newPassword.getBytes())));
		//getLogger().info("Changed administrator password");
		return "OK";
	}
	
	private String checkAuth(String value) {
		if (value.equals(password)) {
			//getLogger().info("Administrator logged in");
			return "PASSWORD OK";
		}	
		//getLogger().info("Administrator authorization rejected");
		return "INVALID PASSWORD";
	}

}
