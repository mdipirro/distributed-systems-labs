package rental;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "RESERVATION")
@NamedQuery(
    name = "findBestCostumers",
    query = "SELECT reservation.carRenter "
            + "FROM Reservation reservation "
            + "WHERE ("
            + " SELECT COUNT(res.id) " // how many reservations for this renter?
            + " FROM Reservation res "
            + " WHERE res.carRenter = reservation.carRenter "
            + ") = ("
            + " SELECT MAX(COUNT(res.id)) " // maximum number of reservations
            + " FROM Reservation res "
            + " GROUP BY res.carRenter"
            + ")"
)
public class Reservation extends Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    @ManyToOne
    private Car car;
    
    /***************
     * CONSTRUCTOR *
     ***************/
    public Reservation() {
        super();
    }
    
    public Reservation(Quote quote, Car car) {
    	super(quote.getCarRenter(), quote.getStartDate(), quote.getEndDate(), 
    		quote.getRentalCompany(), quote.getCarType(), quote.getRentalPrice());
        this.car = car;
    }
    
    /******
     * ID *
     ******/
    
    public Car getCar() {
    	return car;
    }
    
    /*************
     * TO STRING *
     *************/
    
    @Override
    public String toString() {
        return String.format("Reservation for %s from %s to %s at %s\nCar type: %s\tCar: %s\nTotal price: %.2f", 
                getCarRenter(), getStartDate(), getEndDate(), getRentalCompany(), getCarType(), car.getId(), getRentalPrice());
    }
}