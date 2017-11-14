package rental;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.REFRESH;
import static javax.persistence.CascadeType.PERSIST;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "CAR")
@NamedQuery(
    name = "getAvailableCarTypes",
    query = "SELECT DISTINCT car.type "
            + "FROM Car car "
            + "WHERE car.id NOT IN( "
            + "     SELECT res.carId "
            + "     FROM Reservation res "
            + "     WHERE (res.startDate <= :start AND res.endDate >= :start) OR "
            + "           (res.startDate <= :end AND res.endDate >= :end)) "
)
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @ManyToOne(cascade = {PERSIST, MERGE, REFRESH}, fetch = FetchType.EAGER)
    private CarType type;
    
    //@ElementCollection
    @OneToMany(cascade = ALL)
    private Set<Reservation> reservations;

    @ManyToOne
    private CarRentalCompany company;
    /***************
     * CONSTRUCTOR *
     ***************/
    
    public Car() {
        this.reservations = new HashSet<Reservation>();
    }
    
    public Car(int uid, CarType type) {
    	//this.id = uid;
        this.type = type;
        this.reservations = new HashSet<Reservation>();
    }
    
    public Car(int uid, CarType type, CarRentalCompany company) {
    	//this.id = uid;
        this.type = type;
        this.reservations = new HashSet<Reservation>();
        this.company = company;
    }

    /******
     * ID *
     ******/
    public int getId() {
    	return id;
    }
    
    /************
     * CAR TYPE *
     ************/
    public CarType getType() {
        return type;
    }
	
	public void setType(CarType type) {
		this.type = type;
	}
        
    /***************
     * CAR RENTAL COMPANY*
     ****************/    
    public CarRentalCompany getCompany() {
        return company;
    }
    
    public void setCompany(CarRentalCompany company) {
        this.company = company;
    }
        
    /****************
     * RESERVATIONS *
     ****************/

    public boolean isAvailable(Date start, Date end) {
        if(!start.before(end))
            throw new IllegalArgumentException("Illegal given period");

        for(Reservation reservation : reservations) {
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

    public Set<Reservation> getReservations() {
        return reservations;
    }
}