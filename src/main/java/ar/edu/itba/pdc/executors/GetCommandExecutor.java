package ar.edu.itba.pdc.executors;

import java.util.Map;

import ar.edu.itba.pdc.configuration.ConfigurationCommands;
import ar.edu.itba.pdc.filters.NewStatisticsFilter;
import ar.edu.itba.pdc.filters.NewStatisticsFilter.IntervalStatusCode;
import ar.edu.itba.pdc.parser.PDCResponse;
import ar.edu.itba.pdc.parser.PDCResponseJson;

/**
 * Executes the GET operation in PDC protocol
 * 
 * @author grupo 3
 * 
 */
public class GetCommandExecutor extends AbstractCommandExecutor {

	private static GetCommandExecutor instance = null;
	private ConfigurationCommands commandManager;

	public static GetCommandExecutor getInstance() {
		if (instance == null)
			instance = new GetCommandExecutor();
		return instance;
	}

	private GetCommandExecutor() {
		commandManager = ConfigurationCommands.getInstance();
	}

	/**
	 * Returns a message with the response to the command. command=get and value
	 * is the PARAM in PDC protocol
	 */
	public PDCResponse execute(String command, String value) {
		commandManager.saveFile();
		String ans = null;

		if (value.equals("accesses")) {
			int acces = NewStatisticsFilter.getInstance().getAccesses();
			// ans=Integer.toString(NewStatisticsFilter.getInstance().getAccesses());
			PDCResponseJson resp = new PDCResponseJson(200, "PDC/1.0");
			resp.appendData("accesses", acces);
			return resp;
		} else if (value.equals("txbytes")) {
			ans = Integer.toString(NewStatisticsFilter.getInstance()
					.gettxBytes());
			int txbytes = NewStatisticsFilter.getInstance().gettxBytes();
			PDCResponseJson resp = new PDCResponseJson(200, "PDC/1.0");
			resp.appendData("txbytes", txbytes);
			return resp;
		} else if (value.equals("histogram")) {
			Map<Integer, IntervalStatusCode> hist = NewStatisticsFilter
					.getInstance().getHistogram();
			PDCResponseJson resp = new PDCResponseJson(200, "PDC/1.0");
			resp.appendData("histogram", hist);
			return resp;
			// ans = "";
			// for (Entry<Integer, Integer> pairs : hist.entrySet()) {
			// ans = ans + pairs.getKey() + ":" + pairs.getValue() + '\n';
			// }
		} else if (value.equals("statisticsjson")) {
			int acces = NewStatisticsFilter.getInstance().getAccesses();
			int txbytes = NewStatisticsFilter.getInstance().gettxBytes();
			Map<Integer, IntervalStatusCode> hist = NewStatisticsFilter
					.getInstance().getHistogram();
			PDCResponseJson resp = new PDCResponseJson(200, "PDC/1.0");
			resp.appendData("accesses", acces);
			resp.appendData("txbytes", txbytes);
			resp.appendData("histogram", hist);
			return resp;
		}
		PDCResponse resp = new PDCResponse(200, "PDC/1.0");
		resp.appendData(ans);
		return resp;
	}
}
