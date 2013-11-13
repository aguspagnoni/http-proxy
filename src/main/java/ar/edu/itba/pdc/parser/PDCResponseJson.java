package ar.edu.itba.pdc.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import ar.edu.itba.pdc.filters.NewStatisticsFilter.IntervalStatusCode;

public class PDCResponseJson extends PDCResponse{
	JSONObject data=new JSONObject();
	
	public PDCResponseJson(int statuscode,String version) {
		super(statuscode,version);
	}

	@Override
	public String getBody() {
		return data.toString();
	}
	
	public void appendData(String name,Object object){
		data.append(name, object);
	}
	
	public void appendData(String name, Map<Integer,IntervalStatusCode> map){
		Map<Integer,JSONObject> m=new HashMap<Integer, JSONObject>();
		for(Entry<Integer, IntervalStatusCode> e: map.entrySet()){
			m.put(e.getKey(), e.getValue().toJSONObject());
		}
		data.append(name,new JSONObject(m));
	}
}
