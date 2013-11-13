package ar.edu.itba.pdc.filters;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ar.edu.itba.pdc.configuration.ConfigurationCommands;
import ar.edu.itba.pdc.parser.HttpResponse;
import ar.edu.itba.pdc.parser.Message;

public class NewStatisticsFilter implements Filter{
	private static final int DEFAULT_INTERVAL = 120000; // 2 minutos en
	// milisegundos
	private static final int TRANSFER_UNIT = 50;
	private static final int ACCESS_UNIT = 1;
	private static int interval = DEFAULT_INTERVAL;
	private static int byteUnit = TRANSFER_UNIT;
	private static NewStatisticsFilter instance = null;

	@SuppressWarnings("unused")
	private boolean statisticsEnabled = false;
	private long initialStatisticsTime = -1;

	private int accesses = 0;
	private int txbytes = 0;
	private Map<Integer, Integer> statusCode = null;

	public static NewStatisticsFilter getInstance() {
		if (instance == null)
			instance = new NewStatisticsFilter();
		return instance;
	}

	private NewStatisticsFilter() {
		if (statusCode == null) {
			statusCode = new HashMap<Integer, Integer>();
			initialStatisticsTime = System.currentTimeMillis();
			// setInterval(StupidAdminParser.getInterval()); // desde el archivo
			// conf
		}
	}

	public int getAccesses() {
		return this.accesses;
	}

	public int gettxBytes() {
		return this.txbytes;
	}

	public Map<Integer, Integer> getHistogram() {
		return this.statusCode;
	}
	
	public void access(){
		accesses++;
	}
	
	public void incTxBytes(int amount){
		txbytes=txbytes+amount;
	}

	public void addStatusCodeCounter(int statusCode) {
		if (this.statusCode.containsKey(statusCode)) {
			int quantity = this.statusCode.get(statusCode);
			quantity++;
			this.statusCode.put(statusCode, quantity);
		} else {
			this.statusCode.put(statusCode, 1);
		}
	}
	

//	public String execute() {
//		int currInterval = getCurrentInterval() + 1;
//		int globalTotalAccesses = 0, globalTotalByteTransfers = 0;
//		int[] globalAccessByInterval = new int[currInterval], byteTransferByInterval = new int[currInterval];
//		String ans = "";
//		Date date = new Date(System.currentTimeMillis());
//		ans += "Estadistica del proxy - " + date + "\n\n";
//
//		for (PersonalStatistic ps : usersStatistics.values()) {
//			int userTotalAccesses = 0, userTotalBytesTransfered = 0;
//			int[] userAccessByInterval = new int[currInterval], userByteTransferByInterval = new int[currInterval];
//
//			for (Entry<Integer, Integer> access : ps.accessBetweenIntervals
//					.entrySet()) {
//				globalAccessByInterval[access.getKey()] += access.getValue();
//				userAccessByInterval[access.getKey()] += access.getValue();
//				userTotalAccesses += access.getValue();
//			}
//			globalTotalAccesses += userTotalAccesses;
//
//			for (Entry<Integer, Integer> bytesTransfered : ps.bytesBetweenIntervals
//					.entrySet()) {
//				byteTransferByInterval[bytesTransfered.getKey()] += bytesTransfered
//						.getValue();
//				userByteTransferByInterval[bytesTransfered.getKey()] += bytesTransfered
//						.getValue();
//				userTotalBytesTransfered += bytesTransfered.getValue();
//			}
//			globalTotalByteTransfers += userTotalBytesTransfered;
//			if (userTotalAccesses != 0 || userTotalBytesTransfered != 0) {
//				ans += "Estadistica del StatusCode: " + ps.statuscode + "\n\n";
//				ans += "Accesos totales del usuario:    " + userTotalAccesses
//						+ "\n";
//				ans += "Bytes transferidos del usuario: "
//						+ userTotalBytesTransfered + "\n";
//				ans += "Histograma de ACCESOS del usuario: " + "\nINTERVALO ("
//						+ interval / 60000 + " mins)\n";
//
//				ans += printHistogram(userAccessByInterval, currInterval,
//						ACCESS_UNIT);
//				ans += "Histograma de TRANSFERENCIA del StatusCode: "
//						+ ps.statuscode + "\nINTERVALO (" + interval / 60000
//						+ " mins)\t" + "UNIDAD (" + byteUnit + " bytes)\n";
//				;
//				ans += printHistogram(userByteTransferByInterval, currInterval,
//						byteUnit);
//			}
//		}
//		ans += "Estadistica General \n";
//		ans += "ACCESOS totales al sistema: " + globalTotalAccesses + "\n";
//		ans += "Bytes TRANSFERENCIA del sistema: " + globalTotalByteTransfers
//				+ "\n";
//		ans += "Histograma de accesos totales: \n";
//		ans += printHistogram(globalAccessByInterval, currInterval, ACCESS_UNIT);
//		ans += "Histograma de transferencias totales: \n";
//		ans += printHistogram(byteTransferByInterval, currInterval, byteUnit);
//		return ans
//				+ "----------------------------------END OF MESSAGE------------------------------------------\n";
//	}



	public void setInterval(int minutes) {
		interval = minutes * 60 * 1000;
	}

	public void setByteUnit(int byteUnit) {
		this.byteUnit = byteUnit;
	}

	public void enableStatistics() {
		statisticsEnabled = true;
	}

	public void disableStatistics() {
		statisticsEnabled = false;
	}

	private int getCurrentInterval() {
		return (int) ((System.currentTimeMillis() - initialStatisticsTime) / interval);
	}

	

	public boolean filter(Message m) {
		String s = ConfigurationCommands.getInstance()
				.getProperty("statistics");
		if (s != null && s.equals("on")) {
			if (m.getClass().equals(HttpResponse.class)) {
				int statuscode;
				statuscode = ((HttpResponse) m).getCode();
				this.addStatusCodeCounter(statuscode);
				this.access();
				this.incTxBytes(m.getAmountRead());
				
			}

			return true; 

		}
		return false;

	}

}
