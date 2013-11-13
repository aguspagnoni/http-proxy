package ar.edu.itba.pdc.parser;

import java.nio.ByteBuffer;

import ar.edu.itba.pdc.exceptions.AdminException;

public interface AdministratorParser {
	public Message parse(ByteBuffer readBuffer, PDCRequest message)
			throws AdminException, InvalidMessageException;
}
