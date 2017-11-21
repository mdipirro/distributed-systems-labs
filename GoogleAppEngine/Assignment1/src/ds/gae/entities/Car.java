package ds.gae.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.REFRESH;
import static javax.persistence.CascadeType.PERSIST;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.datanucleus.annotations.Unowned;

@Entity
@Table(name = "CAR")
public class Car implements Serializable {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;
	
    private int carId;
       
    //@Unowned
    //@ManyToOne(cascade = {PERSIST, MERGE, REFRESH}, fetch = FetchType.EAGER)
    //private String type;
    
	@OneToMany(cascade = {CascadeType.REMOVE,CascadeType.PERSIST})
    private Set<Reservation> reservations = new HashSet<Reservation>();
    
    /***************
     * CONSTRUCTOR *
     ***************/
    public Car(){}
    public Car(int uid) {
    	this.carId = uid;
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
    
    /************
     * CAR TYPE *
     ************/
    
    /*public String getType() {
        return type;
    }*/

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