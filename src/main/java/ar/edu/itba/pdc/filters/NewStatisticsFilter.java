package ar.edu.itba.pdc.filters;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import ar.edu.itba.pdc.configuration.ConfigurationCommands;
import ar.edu.itba.pdc.parser.HttpRequest;
import ar.edu.itba.pdc.parser.HttpResponse;
import ar.edu.itba.pdc.parser.Message;

/**
 * Filter that stores the statistics about flow message
 * 
 * @author grupo 3
 * 
 */
public class NewStatisticsFilter implements Filter {
	private static final int DEFAULT_INTERVAL = 1000;
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
	private Map<Integer, IntervalStatusCode> statusCode = null;

	public static NewStatisticsFilter getInstance() {
		if (instance == null)
			instance = new NewStatisticsFilter();
		return instance;
	}

	private NewStatisticsFilter() {
		if (statusCode == null) {
			statusCode = new HashMap<Integer, IntervalStatusCode>();
			initialStatisticsTime = System.currentTimeMillis();
			// setInterval(StupidAdminParser.getInterval()); // desde el archivo
			// conf
		}
	}

	/**
	 * Get the proxy's accesses quantity
	 * 
	 * @return
	 */
	public int getAccesses() {
		return this.accesses;
	}

	/**
	 * Get the transfered bytes' amount
	 * 
	 * @return
	 */
	public int gettxBytes() {
		return this.txbytes;
	}

	public double gettxKilobytes() {
		return ((double) this.txbytes) / 1024.0;
	}

	/**
	 * Get the histogram of status codes
	 * 
	 * @return
	 */
	public Map<Integer, IntervalStatusCode> getHistogram() {
		return this.statusCode;
	}

	/**
	 * Registers a proxy's access
	 */
	public void access() {
		accesses++;
	}

	/**
	 * Increments amount of transfered bytes
	 */
	public void incTxBytes(int amount) {
		txbytes = txbytes + amount;
	}

	/**
	 * Register a statusCode
	 * 
	 * @param statusCode
	 */
	public void addStatusCodeCounter(int statusCode) {
		if (this.statusCode.containsKey(statusCode)) {

			IntervalStatusCode isc = this.statusCode.get(statusCode);
			isc.putstatusCode((int) (System.currentTimeMillis() - initialStatisticsTime)
					/ interval);
			this.statusCode.put(statusCode, isc);

		} else {
			IntervalStatusCode isc = new IntervalStatusCode();
			this.statusCode.put(statusCode, isc);

		}
	}

	public void enableStatistics() {
		statisticsEnabled = true;
	}

	public void disableStatistics() {
		statisticsEnabled = false;
	}

	public boolean filter(Message m) {
		String s = ConfigurationCommands.getInstance()
				.getProperty("statistics");
		if (s != null && s.equals("on")) {
			if (m.getClass().equals(HttpResponse.class)) {
				int statuscode;
				statuscode = ((HttpResponse) m).getCode();
				if (statuscode != 0) {
					this.addStatusCodeCounter(statuscode);
					this.access();
					this.incTxBytes(m.getAmountRead());
				}
			}
			if (m.getClass().equals(HttpRequest.class)) {
				this.access();
				this.incTxBytes(m.getAmountRead());
			}

			return true;

		}
		return false;

	}

	/**
	 * 
	 * @author grupo 3
	 * 
	 */
	public static class IntervalStatusCode {
		Map<Integer, Integer> statuscodeMapping = new HashMap<Integer, Integer>();

		public void putstatusCode(int intervalNumber) {
			Integer qty = statuscodeMapping.get(intervalNumber);
			if (qty != null) {
				qty++;
			} else {
				qty = 1;
			}
			statuscodeMapping.put(intervalNumber, qty);
		}

		public JSONObject toJSONObject() {
			return new JSONObject(statuscodeMapping);
		}
	}

}
