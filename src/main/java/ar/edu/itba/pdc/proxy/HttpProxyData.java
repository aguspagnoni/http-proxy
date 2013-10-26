package ar.edu.itba.pdc.proxy;


import java.util.HashMap;
import java.util.Map;
import ar.edu.itba.pdc.parser.HttpResponse;


public class HttpProxyData {
	private static HttpProxyData instance;
	
	private int cant_access;
	private int tx_bytes;
	private Map<Integer, Integer> histogram=new HashMap<Integer, Integer>();
	private Map<String,String> authentication=new HashMap<String, String>(); //es medio rancio y podría cambiarse por algo como crear una clase de User, pero no sé si es mucho para este caso
	
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
	
	public void registerBytes(int sizebuffer){
		tx_bytes+=sizebuffer;
	}
	
	public int getTxBytes(){
		return tx_bytes;
	}
	
	public boolean getAuthentication(String user, String pass){
		String us=authentication.get(user);
		if(us!=null && us.equals(pass)){
			return true;
		}
		return false;
	}
	
	//falta implementarlo: el boolean significa si es un filter Accept o Reject
	public boolean addFilter(String url, boolean accept){
		return false;
	}
}
