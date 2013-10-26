package ar.edu.itba.pdc.filters;

import ar.edu.itba.pdc.parser.Message;


public interface Filter {
	public boolean filter(Message m);

}
