package ar.edu.itba.pdc.parser;

import org.json.JSONObject;

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
}
