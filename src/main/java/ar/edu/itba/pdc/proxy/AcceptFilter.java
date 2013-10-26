package ar.edu.itba.pdc.proxy;

public class AcceptFilter extends Filter{

	public AcceptFilter(String url) {
		super(url);

	}

	@Override
	public boolean filter(String url) {
		if(getUrl().equals(url)){
			return true;			
		}
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AcceptFilter && obj!=null){
			return obj.equals(this);
		}
		return false;
		
	}

}
