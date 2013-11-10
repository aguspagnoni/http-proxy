package ar.edu.itba.pdc.executors;

import org.apache.commons.codec.binary.Base64;

import ar.edu.itba.pdc.configuration.ConfigurationCommands;
import ar.edu.itba.pdc.parser.PDCResponse;


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
	
	public PDCResponse execute(String command, String value) {
		if (command.equals("auth")){
			return (checkAuth(value))?null:new PDCResponse(401, "PDC/1.0");			
		}
//		else if (command.equals("changePassword"))
//			return passwordChange(value);
		return null;
	}
	
	private String passwordChange(String newPassword) {
		this.password = newPassword;
		this.commandManager.setProperty("password", new String(Base64.encodeBase64(newPassword.getBytes())));
		//getLogger().info("Changed administrator password");
		return "OK";
	}
	
	private boolean checkAuth(String value) {
		if (value.equals(password)) {
			//getLogger().info("Administrator logged in");
			return true;
		}	
		//getLogger().info("Administrator authorization rejected");
		return false;
	}

}
