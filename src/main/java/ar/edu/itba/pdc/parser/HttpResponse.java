package ar.edu.itba.pdc.parser;

public class HttpResponse extends Message {
	
	int code;
	//hereda mapa de headers
	//hereda el body

	public int getCode(){
		return code;
	}
}
