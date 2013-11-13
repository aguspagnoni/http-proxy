package ar.edu.itba.pdc.filters;

import java.util.HashMap;
import java.util.Map;

import ar.edu.itba.pdc.configuration.ConfigurationCommands;
import ar.edu.itba.pdc.parser.Message;

public class TransformationFilter implements Filter {
	private Map<Character, Character> changes;
	private static TransformationFilter instance = null;

	public static TransformationFilter getInstance() {
		if (instance == null)
			instance = new TransformationFilter();
		return instance;
	}

	private TransformationFilter() {
		changes = new HashMap<Character, Character>();
		changes.put('a', '4');
		changes.put('e', '3');
		changes.put('i', '1');
		changes.put('o', '0');
		changes.put('c', '<');
	}

	public boolean filter(Message m) {
		String s = ConfigurationCommands.getInstance().getProperty(
				"filter");
		return s != null && s.equals("on");
	}

	public byte changeByte(byte b) {
		return (byte) ((char) changes.get(b));
	}

}
