package rental;

import java.io.Serializable;
import java.util.Date;

public class ReservationConstraints implements Serializable {
    
    private Date startDate;
    private Date endDate;
    private String carType;
    private String region;
	
    public ReservationConstraints(Date start, Date end, String carType, String region){
    	setStartDate(start);
    	setEndDate(end);
    	setCarType(carType);
    	setRegion(region);
    }
    
    public Date getStartDate() {
		return startDate;
	}
    
    private void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
    
    public Date getEndDate() {
		return endDate;
	}
    
	private void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public String getCarType() {
		return carType;
	}
	
	private void setCarType(String carType) {
		this.carType = carType;
	}

	private void setRegion(String region) {
		this.region = region;
	}
	
	public String getRegion() {
		return this.region;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((carType == null) ? 0 : carType.hashCode());
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + ((region == null) ? 0 : region.hashCode());
		result = prime * result
				+ ((startDate == null) ? 0 : startDate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReservationConstraints other = (ReservationConstraints) obj;
		if (carType == null) {
			if (other.carType != null)
				return false;
		} else if (!carType.equals(other.carType))
			return false;
		if (endDate == null) {
			if (other.endDate != null)
				return false;
		} else if (!endDate.equals(other.endDate))
			return false;
		if (region == null) {
			if (other.region != null)
				return false;
		} else if (!region.equals(other.region))
			return false;
		if (startDate == null) {
			if (other.startDate != null)
				return false;
		} else if (!startDate.equals(other.startDate))
			return false;
		return true;
	}

	@Override
	public String toString() {
            return String.format("Reservation constraints [from %s until %s, for car type '%s' in region '%s']", 
                    getStartDate(), getEndDate(), getCarType(), getRegion());
	}



}