package ar.edu.itba.pdc.filters;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ar.edu.itba.pdc.parser.Message;

public class StatisticsFilter implements Filter {

	private static final int DEFAULT_INTERVAL = 120000; // 2 minutos en
														// milisegundos
	private static final int TRANSFER_UNIT = 50;
	private static final int ACCESS_UNIT = 1;
	private static int interval = DEFAULT_INTERVAL;
	private static int byteUnit = TRANSFER_UNIT;
	private static StatisticsFilter instance;
	@SuppressWarnings("unused")
	private boolean statisticsEnabled = false;
	private long initialStatisticsTime = -1;

	private Map<String, PersonalStatistic> usersStatistics = null;

	public static StatisticsFilter getInstance() {
		if (instance == null)
			instance = new StatisticsFilter();
		return instance;
	}

	private StatisticsFilter() {
		if (usersStatistics == null) {
			usersStatistics = new HashMap<String, PersonalStatistic>();
			initialStatisticsTime = System.currentTimeMillis();
			// setInterval(AdminParser.getInterval()) // desde el archivo conf
		}
	}

	public String execute() {
		int currInterval = getCurrentInterval() + 1;
		int globalTotalAccesses = 0, globalTotalByteTransfers = 0;
		int[] globalAccessByInterval = new int[currInterval], byteTransferByInterval = new int[currInterval];
		String ans = "";
		Date date = new Date(System.currentTimeMillis());
		ans += "Estadistica del proxy - " + date + "\n\n";

		for (PersonalStatistic ps : usersStatistics.values()) {
			int userTotalAccesses = 0, userTotalBytesTransfered = 0;
			int[] userAccessByInterval = new int[currInterval], userByteTransferByInterval = new int[currInterval];

			for (Entry<Integer, Integer> access : ps.accessBetweenIntervals
					.entrySet()) {
				globalAccessByInterval[access.getKey()] += access.getValue();
				userAccessByInterval[access.getKey()] += access.getValue();
				userTotalAccesses += access.getValue();
			}
			globalTotalAccesses += userTotalAccesses;

			for (Entry<Integer, Integer> bytesTransfered : ps.bytesBetweenIntervals
					.entrySet()) {
				byteTransferByInterval[bytesTransfered.getKey()] += bytesTransfered
						.getValue();
				userByteTransferByInterval[bytesTransfered.getKey()] += bytesTransfered
						.getValue();
				userTotalBytesTransfered += bytesTransfered.getValue();
			}
			globalTotalByteTransfers += userTotalBytesTransfered;
			if (userTotalAccesses != 0 || userTotalBytesTransfered != 0) {
				ans += "Estadistica del Usuario: " + ps.clientaddr + "\n\n";
				ans += "Accesos totales del usuario:    " + userTotalAccesses
						+ "\n";
				ans += "Bytes transferidos del usuario: "
						+ userTotalBytesTransfered + "\n";
				ans += "Histograma de ACCESOS del usuario: " + "\nINTERVALO ("
						+ interval / 60000 + " mins)\n";

				ans += printHistogram(userAccessByInterval, currInterval,
						ACCESS_UNIT);
				ans += "Histograma de TRANSFERENCIA del usuario: "
						+ ps.clientaddr + "\nINTERVALO (" + interval / 60000
						+ " mins)\t" + "UNIDAD (" + byteUnit + " bytes)\n";
				;
				ans += printHistogram(userByteTransferByInterval, currInterval,
						byteUnit);
			}
		}
		ans += "Estadistica General \n";
		ans += "ACCESOS totales al sistema: " + globalTotalAccesses + "\n";
		ans += "Bytes TRANSFERENCIA del sistema: " + globalTotalByteTransfers
				+ "\n";
		ans += "Histograma de accesos totales: \n";
		ans += printHistogram(globalAccessByInterval, currInterval, ACCESS_UNIT);
		ans += "Histograma de transferencias totales: \n";
		ans += printHistogram(byteTransferByInterval, currInterval, byteUnit);
		return ans
				+ "----------------------------------END OF MESSAGE------------------------------------------\n";
	}

	public String executeLatest() {
		String ans = "Usuarios receientemente activos:\n\n";
		int currInterval = getCurrentInterval();

		for (PersonalStatistic ps : usersStatistics.values()) {
			if (ps.bytesBetweenIntervals.containsKey(currInterval)) {
				ans += ps.clientaddr + "\n";
			}
		}
		return ans
				+ "----------------------------------END OF MESSAGE------------------------------------------\n";
	}

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

	private String printHistogram(int[] array, int interval, int unit) {
		String out = "";
		for (int i = 0; i < array.length; i++) {
			out += i + ": ";
			int aux = 0;
			while (aux + unit <= array[i]) {
				out += "*";
				aux += unit;
			}
			out += "\n";
		}
		return out + "\n";
	}

	/* inicio clase interna */

	private class PersonalStatistic {

		Map<Integer, Integer> accessBetweenIntervals = new HashMap<Integer, Integer>();
		Map<Integer, Integer> bytesBetweenIntervals = new HashMap<Integer, Integer>();
		String clientaddr = null;

		PersonalStatistic(String clientaddr) {
			this.clientaddr = clientaddr;
		}

		private void applyFilter(Message m) {
			int position = StatisticsFilter.this.getCurrentInterval();
			if (m.getBody() != null) {
				if (!bytesBetweenIntervals.containsKey(position))
					bytesBetweenIntervals.put(position, m.getBody().length());
				else
					bytesBetweenIntervals.put(position,
							bytesBetweenIntervals.get(position)
									+ m.getBody().length());
			}
		}

	}

	/* fin clase interna */

	public boolean filter(Message m) {
		String clientaddr;
		clientaddr = m.getClientaddr();
		if (!usersStatistics.containsKey(clientaddr)) {
			usersStatistics.put(clientaddr, new PersonalStatistic(clientaddr));
		}
		usersStatistics.get(clientaddr).applyFilter(m);
		return true; // ?????????

	}
}