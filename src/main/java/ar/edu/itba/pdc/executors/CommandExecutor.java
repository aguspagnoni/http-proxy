package ar.edu.itba.pdc.executors;

import ar.edu.itba.pdc.parser.PDCResponse;

public interface CommandExecutor {

	public PDCResponse execute(String command, String value);
}
