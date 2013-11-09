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
		commandTypes.put("statistics", BooleanCommandExecutor.getInstance());
		commandTypes.put("getStatistics", GetCommandExecutor.getInstance());
		commandTypes
				.put("transformation", BooleanCommandExecutor.getInstance());
		RemoveFromListCommandExecutor.getInstance();
		commandTypes.put("auth", AuthService.getInstance());
		commandTypes.put("interval", ValueCommandExecutor.getInstance());
		commandTypes.put("byteUnit", ValueCommandExecutor.getInstance());
	}
	
	public Message parse(ByteBuffer readBuffer, Message message)
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
//				readBuffer.compact();
				break;
			case Header:
	
//				readBuffer.flip();
				do {
					i = 0;
					while ((b = readBuffer.get()) != '\n' && readBuffer.hasRemaining())
						auxBuf[i++] = b;
					
					if (i > 1) // last case
						message.addHeader(new String(auxBuf, 0, i-1));
					
				} while (i > 1); // if is 1 it reached \r\n line
				
				message.state = ParsingState.Body;
//				readBuffer.compact();
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
				return message; // hasta q no este lo de la transformacion se mmanda asi como viene
			case Complete:
				readBuffer.rewind();
				return message;
			}
		}
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
