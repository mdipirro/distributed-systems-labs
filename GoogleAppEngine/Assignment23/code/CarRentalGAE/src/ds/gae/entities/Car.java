package ds.gae.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.google.appengine.api.datastore.Key;

@Entity
@Table(name = "CAR")
public class Car implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3594297949432522842L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;
	
    private int carId;
    
    private String company;
    private String type;
    
	@OneToMany(cascade = {CascadeType.REMOVE,CascadeType.PERSIST})
    private Set<Reservation> reservations = new HashSet<Reservation>();
    
    /***************
     * CONSTRUCTOR *
     ***************/
    public Car(){}
    public Car(int uid, String company, String type) {
    	this.carId = uid;
    	this.company = company;
    	this.type = type;
    }
    /*public Car(int uid, String type) {
    	this.carId = uid;
        //this.type = type;
        this.reservations = new HashSet<Reservation>();
    }*/

    /******
     * ID *
     ******/
    
    public Key getKey() {
    	return key;
    }
    
    public int getId() {
    	return carId;
    }
    
    /***********
     * COMPANY
     ***********/
    
    public String getCompany(){
    	return company;
    }
    
    /************
     * CAR TYPE *
     ************/
    
    public String getType() {
        return type;
    }

    /****************
     * RESERVATIONS *
     ****************/
    
    public Set<Reservation> getReservations() {
    	return reservations;
    }

    public boolean isAvailable(Date start, Date end) {
        if(!start.before(end))
            throw new IllegalArgumentException("Illegal given period");

        if (reservations == null) {
        	reservations = new HashSet<Reservation>();
        }
        
        for(Reservation reservation : getReservations()) {
            if(reservation.getEndDate().before(start) || reservation.getStartDate().after(end))
                continue;
            return false;
        }
        return true;
    }
    
    public void addReservation(Reservation res) {
        reservations.add(res);
    }
    
    public void removeReservation(Reservation reservation) {
        // equals-method for Reservation is required!
        reservations.remove(reservation);
    }
}