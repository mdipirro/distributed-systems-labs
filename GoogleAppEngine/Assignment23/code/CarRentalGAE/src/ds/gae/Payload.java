package ds.gae;

import java.io.Serializable;
import java.util.List;

import ds.gae.entities.Quote;

public class Payload implements Serializable {
	private static final long serialVersionUID = 1815146919488221603L;
	
	private List<Quote> quotes;
	private String id;
	
	public Payload(String id, List<Quote> quotes) {
		this.id = id;
		this.quotes = quotes;
	}
	
	public List<Quote> getQuotes() {
		return quotes;
	}
	
	public String getID() {
		return id;
	}
}
