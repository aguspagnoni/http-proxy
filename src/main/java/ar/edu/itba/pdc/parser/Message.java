package ar.edu.itba.pdc.parser;

import java.util.HashMap;
import java.util.Map;

public abstract class Message {
	
	protected Map<String, String> headers = new HashMap<String, String>();
	protected String body = ""; //TODO cambiar todo String por ByteBuffer!!!
	

}
