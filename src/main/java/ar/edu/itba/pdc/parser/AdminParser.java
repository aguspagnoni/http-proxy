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
import ar.edu.itba.pdc.parser.enumerations.ParsingState;

public class AdminParser {
	private Map<String, CommandExecutor> commandTypes = new HashMap<String, CommandExecutor>();
	private ConfigurationCommands commandManager;
	
	public AdminParser(){
		commandManager = ConfigurationCommands.getInstance();
	//	commandTypes.put("statistics", BooleanCommandExecutor.getInstance()); //que se supone que hace esto?
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
	
	public Message parse(ByteBuffer readBuffer, PDCRequest message)
			throws BadSyntaxException, InvalidMessageException {

		if (message == null)
			throw new InvalidMessageException();
		byte[] auxBuf = ByteBuffer.allocate(8192).array();
		readBuffer.flip();
		
		byte b;
		int i = 0;
		while (true) {
			
			switch (message.state) {
			case Head:
				while ((b = readBuffer.get()) != '\n' && readBuffer.hasRemaining())
					auxBuf[i++] = b;
	
				message.firstLine = new String(auxBuf);
				if (message.firstLine == null || message.firstLine.length() == 0)
					return null; // empty message
				message.fillHead();
				message.state = ParsingState.Header;
				break;
			case Header:
	
				do {
					i = 0;
					while ((b = readBuffer.get()) != '\n' && readBuffer.hasRemaining())
						auxBuf[i++] = b;
					
					if (i > 1) // last case
						message.addHeader(new String(auxBuf, 0, i-1));
					
				} while (i > 1); // if is 1 it reached \r\n line
				
				message.state = ParsingState.Body;
				break;
			case Body:
					// filter.transform()
	//			boolean transformationOn = filter.applyTransformation && message.headers.get("content-type").contains("text/plain");
	//			if (transformationOn) {
	//				ByteBuffer transformedBuffer = ByteBuffer.allocate(readBuffer.capacity());
					while (readBuffer.hasRemaining() && (b = readBuffer.get()) != -1 && !message.isFinished()); // cierre de conexion (-1) es una forma de indicar q el mensaje se termino.
//						readBuffer.put(readBuffer.arrayOffset() + i, b);
	//			}
				
				readBuffer.rewind();
				return message; 
			case Complete:
				PDCResponse response=message.parseMessage(readBuffer, i); //aca es donde se hace la logica del parseo y se ejecutan los comandos
				readBuffer.rewind();
				return response;
			}
		}
	}
	
	
}
