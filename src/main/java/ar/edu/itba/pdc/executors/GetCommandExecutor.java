package ar.edu.itba.pdc.executors;

import java.text.DecimalFormat;
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
	@SuppressWarnings("unused")
	public PDCResponse execute(String command, String value) {
		commandManager.saveFile();
		String ans = null;

		NewStatisticsFilter StatisticFilterInstance = NewStatisticsFilter
				.getInstance();
		if (value.equals("accesses")) {
			int acces = StatisticFilterInstance.getAccesses();
			PDCResponseJson resp = new PDCResponseJson(200, "PDC/1.0");
			resp.appendData("accesses", acces);
			return resp;
		} else if (value.equals("txbytes")) {
			ans = Integer.toString(StatisticFilterInstance.gettxBytes());
			int txbytes = StatisticFilterInstance.gettxBytes();
			PDCResponseJson resp = new PDCResponseJson(200, "PDC/1.0");
			resp.appendData("txbytes", txbytes);
			return resp;
		} else if (value.equals("histogram")) {
			Map<Integer, IntervalStatusCode> hist = StatisticFilterInstance
					.getHistogram();
			PDCResponseJson resp = new PDCResponseJson(200, "PDC/1.0");
			resp.appendData("histogram", hist);
			return resp;

		} else if (value.equals("statisticsjson")) {
			int acces = StatisticFilterInstance.getAccesses();
			int txbytes = StatisticFilterInstance.gettxBytes();
			double txtkbytes = txbytes / 1024.0;
			DecimalFormat df = new DecimalFormat("#.##");
			String s = df.format(txtkbytes);
			Map<Integer, IntervalStatusCode> hist = StatisticFilterInstance
					.getHistogram();
			PDCResponseJson resp = new PDCResponseJson(200, "PDC/1.0");
			resp.appendData("accesses", acces);
			resp.appendData("txbytes", txbytes);
			// resp.appendData("txkbytes", s); //IF YOU WANT IT IN KBytes
			resp.appendData("histogram", hist);
			return resp;
		}
		PDCResponse resp = new PDCResponse(200, "PDC/1.0");
		resp.appendData(ans);
		return resp;
	}
}
