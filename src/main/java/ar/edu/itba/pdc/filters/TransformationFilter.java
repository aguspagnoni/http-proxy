package ar.edu.itba.pdc.filters;

import java.util.HashMap;
import java.util.Map;

import ar.edu.itba.pdc.configuration.ConfigurationCommands;
import ar.edu.itba.pdc.parser.Message;

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
		changes.put('c', "&lt;");
	}

	public boolean filter(Message m) {
		String s = ConfigurationCommands.getInstance().getProperty(
				"filter");
		if (s != null && s.equals("enabled")) {

			// String body = m.;
			// if (body != null) {
			// StringBuffer sb = new StringBuffer(body.length());
			// for (int i = 0; i < body.length(); i++) {
			// if (changes.containsKey(body.charAt(i)))
			// sb.append(changes.get(body.charAt(i)));
			// else
			// sb.append(body.charAt(i));
			// }
			// m.setBody(sb.toString());
			// }
			//
		}
		return true; // ?????????
	}

}
