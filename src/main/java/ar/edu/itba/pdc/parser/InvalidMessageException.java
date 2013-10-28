package ar.edu.itba.pdc.parser;

@SuppressWarnings("serial")
public class InvalidMessageException extends Exception {
	
	@Override
	public String toString() {
		return "a new instance of Message is needed.";
	}

}
