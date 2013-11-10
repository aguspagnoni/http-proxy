package ar.edu.itba.pdc.executors;

import java.util.Map;
import java.util.Map.Entry;

import ar.edu.itba.pdc.configuration.ConfigurationCommands;
import ar.edu.itba.pdc.filters.StatisticsFilter;
import ar.edu.itba.pdc.parser.PDCResponse;
import ar.edu.itba.pdc.proxy.HttpProxyData;

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

	public PDCResponse execute(String command, String value) {
		commandManager.saveFile();
		String ans = null;
		
		if (value.equals("accesses")) {
				ans=Integer.toString(StatisticsFilter.getInstance().getAccesses());
			
		} else if(value.equals("txbytes")){
			ans=Integer.toString(StatisticsFilter.getInstance().gettxBytes());
		}
		else if(value.equals("histogram")){
			Map<Integer,Integer> hist=StatisticsFilter.getInstance().getHistogram();
			ans="";
			for(Entry<Integer, Integer> pairs:hist.entrySet()){
				ans=ans+pairs.getKey()+":"+pairs.getValue()+'\n';
			}
		}
		PDCResponse resp= new PDCResponse(200, "PDC/1.0");
		resp.appendData(ans);
		return resp;
	}
}
