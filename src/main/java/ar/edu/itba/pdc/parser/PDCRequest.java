package ar.edu.itba.pdc.parser;

public class PDCRequest extends Message{

    private String operation;
    private String param;
    private String version;
    
	@Override
	public boolean isFinished() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void fillHead() {
		 String[] aux = firstLine.toLowerCase().split(" "); 
         if (firstLine != null && aux.length == 3) {
                 this.operation = aux[0];
                 this.param = aux[1];
                 this.version = aux[2];
         }
	}
	

}
