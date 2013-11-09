package ar.edu.itba.pdc.parser;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import ar.edu.itba.pdc.configuration.ConfigurationCommands;
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
    private HashSet<String> implementedMethods = new HashSet<String>();
	private Map<String, CommandExecutor> commandTypes = new HashMap<String, CommandExecutor>();
	private ConfigurationCommands commandManager;
	
	public PDCRequest(){
		implementedMethods.add("get");
		implementedMethods.add("add");
		implementedMethods.add("del");
		commandManager = ConfigurationCommands.getInstance();
		commandTypes.put("statistics", BooleanCommandExecutor.getInstance());
		commandTypes.put("gethistogram", GetCommandExecutor.getInstance());
		commandTypes.put("getaccesses", GetCommandExecutor.getInstance());
		commandTypes.put("gettxbytes", GetCommandExecutor.getInstance());
		commandTypes
				.put("transformation", BooleanCommandExecutor.getInstance());
		RemoveFromListCommandExecutor.getInstance();
		commandTypes.put("authentication", AuthService.getInstance());
		
		commandTypes.put("interval", ValueCommandExecutor.getInstance());
		commandTypes.put("byteUnit", ValueCommandExecutor.getInstance());
		
		commandTypes.put("addfilter", null);
		commandTypes.put("delfilter", null);
	}
	
	@Override
	public boolean isFinished() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void fillHead() {
		 String[] aux = firstLine.toLowerCase().split(" "); 
         if (firstLine != null && aux.length == 3) {
                 this.operation = aux[0].toLowerCase();
                 this.param = aux[1].toLowerCase();
                 this.version = aux[2];
         }
	}
	
	public String parseMessage(ByteBuffer readBuffer, int bytesRead){
		if(implementedMethods.contains(operation)){
			
		}
		return null;
	}
	

}
