package ar.edu.itba.pdc.proxy;

import java.util.HashMap;
import java.util.Map;

import ar.edu.itba.pdc.parser.HttpResponse;
import ar.edu.itba.pdc.parser.Message;

public class HttpProxyData {
	private static HttpProxyData instance;
	
	private int cant_access;
	private int tx_bytes;
	private Map<Integer, Integer> histogram=new HashMap<Integer, Integer>();
	
	public synchronized HttpProxyData getInstance(){
		if(instance==null){
			instance=  new HttpProxyData();
		}
		return instance;
	}
	
	public void access(){
		cant_access++;
	}
	
	public boolean registerHistogram(HttpResponse resp){
		int code=resp.getCode();
		if(code<0){
			return false;
		}
		Integer value= histogram.get(code);
		if(value==null){
			value=0;
		}
		value++;
		histogram.put(code, value);
		return true;
	}
	
	public int getAccesses(){
		return cant_access;
	}
	
	public Map<Integer, Integer> getHistogram(){
		return histogram;
	}
}
