package ar.edu.itba.pdc.proxy;

//hay que chequear en que paquete va esta clase

public abstract class Filter {
	String url;

	public Filter(String url) {
		if(url==null|| url.isEmpty()){
			throw new IllegalArgumentException();
		}
		this.url = url;
	}
	
	//dada una url hace el filtro correspondiente (aceptarla o rechazarla)
	//y devuelve true si paso el filtrado y false sino
	public abstract boolean filter(String url);
	
	public String getUrl(){
		return this.url;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Filter && obj!=null){
			Filter fil=(Filter)obj;
			return url.equals(fil.getUrl());
		}
		return false;
		
	}
}
