package ar.edu.itba.pdc.filters;

import ar.edu.itba.pdc.parser.Message;

/**
 * Models the behaviour off a Message's filter
 * @author grupo 3
 *
 */
public interface Filter {
	public boolean filter(Message m);

}
