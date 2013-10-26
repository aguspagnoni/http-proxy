package ar.edu.itba.pdc.proxy;

public class RejectFilter extends Filter{

	public RejectFilter(String url) {
		super(url);
	}

	@Override
	public boolean filter(String url) {
		if(getUrl().equals(url)){
			return false;			
		}
		return true;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RejectFilter && obj!=null){
			return obj.equals(this);
		}
		return false;
		
	}

}
