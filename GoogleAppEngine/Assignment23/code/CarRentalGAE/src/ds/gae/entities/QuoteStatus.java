package ds.gae.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "QUOTE_STATUS")
public class QuoteStatus {
	enum Status {
		QUEUED,
		IN_PROCESSING,
		CONFIRMED,
		FAILED
	}
	
	@Id
	private Long id;
	
	private String renter;
	private Status status;
	
	public QuoteStatus() {
		this(null, Status.QUEUED);
	}
	public QuoteStatus(String renter) {
		this(renter, Status.QUEUED);
	}
	public QuoteStatus(String renter, Status status) {
		this.renter = renter;
		this.status = status;
	}
	
	public String getRenter() {
		return renter;
	}
	
	public void setRenter(String renter) {
		this.renter = renter;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
}
