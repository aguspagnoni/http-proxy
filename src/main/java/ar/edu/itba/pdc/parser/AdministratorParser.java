package ar.edu.itba.pdc.parser;

import java.nio.ByteBuffer;

import ar.edu.itba.pdc.exceptions.BadSyntaxException;

public interface AdministratorParser {
	public Message parse(ByteBuffer readBuffer, PDCRequest message)
			throws BadSyntaxException, InvalidMessageException;
}
