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

public class PDCRequest extends Message{

    private String operation;
    private String param;
    private String version;    
	private Map<String, CommandExecutor> commandTypes = new HashMap<String, CommandExecutor>();
	private ConfigurationCommands commandManager;
	
	public PDCRequest(){
		
		commandManager = ConfigurationCommands.getInstance();
//		commandTypes.put("statistics", BooleanCommandExecutor.getInstance());
		commandTypes.put("gethistogram", GetCommandExecutor.getInstance());
		commandTypes.put("getaccesses", GetCommandExecutor.getInstance());
		commandTypes.put("gettxbytes", GetCommandExecutor.getInstance());
		commandTypes
				.put("transformation", BooleanCommandExecutor.getInstance());
		RemoveFromListCommandExecutor.getInstance();
		commandTypes.put("authentication", AuthService.getInstance());
		
//		commandTypes.put("interval", ValueCommandExecutor.getInstance());
//		commandTypes.put("byteUnit", ValueCommandExecutor.getInstance());
		
		commandTypes.put("addfilter", null);
		commandTypes.put("delfilter", null);
	}
	
	@Override
	public boolean isFinished() {
		if(operation!=null && param!=null && version!=null){
			return true;
		}
		return false;
	}

	@Override
	public void fillHead() {
		 String[] aux = firstLine.toLowerCase().split(" "); 
         if (firstLine != null && aux.length == 3) {
                 this.operation = aux[0].toLowerCase().trim();
                 this.param = aux[1].toLowerCase().trim();
                 this.version = aux[2];
         }
	}
	
	public PDCResponse parseMessage(ByteBuffer readBuffer, int bytesRead){
		
		
		if(!version.equals("PDC/1.0")){
			return new PDCResponse(404, "PDC/1.0");
			//aca vendria error 404 NOT FOUND
		}
		
		if(operation!=null && operation.equals("get")){
			if(param!=null){
				if(!commandTypes.containsKey(operation+param)){
					return new PDCResponse(404, "PDC/1.0");
					//aca vendria error 404 NOT FOUND
				}
				if(bytesRead!=0){
					return new PDCResponse(420, "PDC/1.0");  //la DATA en get tiene que estar vacia
					//aca vendria error 420 CORRUPTED DATA
				}
						
			}
			else{
				return new PDCResponse(400, "PDC/1.0");
				//400 BAD REQUEST
			}
			
		}
		else if(operation!=null && (operation.equals("add") || operation.equals("del"))){
			if(!commandTypes.containsKey(operation+param)){
				return new PDCResponse(404, "PDC/1.0");
				//aca vendria error 404 NOT FOUND
			}
			if(!headers.containsKey("authentication")){
				return new PDCResponse(401, "PDC/1.0"); //falta autenticacion
				//aca vendria error 401 UNAUTHORIZED
			}
			if(bytesRead==0){
				return new PDCResponse(420, "PDC/1.0"); // la DATA no puede estar vacia
				//aca vendria error 420 CORRUPTED DATA
			}
			
			
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
		PDCResponse responseToAdmin=null;
		if(!operation.equals("get")){
			responseToAdmin=commandTypes.get(operation+param).execute(operation, param);
		}
		else{
			String[] auth=headers.get("authentication").split(";");
			responseToAdmin=commandTypes.get("authentication").execute("auth", auth[1]); //no estamos contemplando el usuario, habría que ver de cambiarlo en el executor o ver cómo hacemos
			if(responseToAdmin!=null){
				responseToAdmin=commandTypes.get(operation+param).execute(command, value); //ver como sería esto
			}
		}
		if(responseToAdmin!=null){
			commandManager.saveFile();
		} else {
			throw new BadSyntaxException();
		}
		
		
		return responseToAdmin;
	}

	

}
