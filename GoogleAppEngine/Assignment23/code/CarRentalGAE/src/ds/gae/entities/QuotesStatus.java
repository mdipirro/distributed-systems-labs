package ds.gae.entities;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity
@Table(name = "QUOTES_STATUS")
@NamedQuery(
	name = "getStatusByRenter",
	query = "SELECT status "
			+ "FROM QuotesStatus status "
			+ "WHERE status.renter = :renter" 
)
public class QuotesStatus implements Serializable {
	private static final long serialVersionUID = 1246798824996972259L;

	public enum Status {
		QUEUED,
		IN_PROCESSING,
		CONFIRMED,
		FAILED
	}
	
	@Id
	private String id;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date issuedOn;
	
	private String renter;
	private Status status;
	
	public QuotesStatus() {
		this(null, Status.QUEUED);
	}
	public QuotesStatus(String renter) {
		this(renter, Status.QUEUED);
	}
	public QuotesStatus(String renter, Status status) {
		this.renter = renter;
		this.status = status;
		this.issuedOn = Calendar.getInstance().getTime();
		this.id = renter + System.nanoTime();
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
	public String getId() {
		return id;
	}
	public Date getIssueDate() {
		return issuedOn;
	}
	public String toString() {
		switch (status) {
		case QUEUED:
			return "Waiting";
		case IN_PROCESSING:
			return "In Processing";
		case CONFIRMED:
			return "Confirmed";
		case FAILED:
			return "Failed";
		}
		return "";
	}
}
