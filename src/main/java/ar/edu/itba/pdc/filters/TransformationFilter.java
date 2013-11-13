package ar.edu.itba.pdc.filters;

import java.util.HashMap;
import java.util.Map;

import ar.edu.itba.pdc.configuration.ConfigurationCommands;
import ar.edu.itba.pdc.parser.Message;

/**
 * Filter that transform text/plain into 133t format
 * @author grupo 3
 *
 */
public class TransformationFilter implements Filter {
	private Map<Character, String> changes;
	private static TransformationFilter instance = null;

	public static TransformationFilter getInstance() {
		if (instance == null)
			instance = new TransformationFilter();
		return instance;
	}

	private TransformationFilter() {
		changes = new HashMap<Character, String>();
		changes.put('a', "4");
		changes.put('e', "3");
		changes.put('i', "1");
		changes.put('o', "0");
		changes.put('c', "<");
	}

	public boolean filter(Message m) {
		String s = ConfigurationCommands.getInstance().getProperty("filter");
		return s != null && s.equals("on");
	}

	/**
	 * transform a byte into another one according to 133t format
	 * @param b
	 * @return
	 */
	public String changeByte(byte b) {
		System.out.println(((char) b));
		if (changes.get((char) b) != null) {
			System.out.println((changes.get((char) b)));
		}
		String s = "";
		s = s + (char) b;
		return changes.get((char) b) != null ? changes.get((char) (b)) : s;
	}
}
