package rental;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "RESERVATION")
@NamedQueries({
    @NamedQuery(
        name = "findBestCostumers",
        query = "SELECT r.carRenter"
                + "FROM Reservation r"
                + "WHERE ("
                + " SELECT COUNT(res.id)" // how many reservations for this renter?
                + " FROM Reservation res"
                + " WHERE res.carRenter = r.carRenter"
                + ") = ("
                + " SELECT MAX(COUNT(res.id))" // maximum number of reservations
                + " FROM Reservation res"
                + " GROUP BY res.carRenter"
                + ")"
    ),
    @NamedQuery(
        name = "findMostPopularCarType",
        query = "SELECT t"
                + "FROM Reservation r, CarType t "
                + "WHERE r.rentalCompany = :companyName AND"
                +"       t.companyName = :companyName AND"
                + "      COUNT(t) = (   SELECT MAX(COUNT(t))"
                + "                     FROM Reservation r, CarType t"
                + "                     WHERE   r.rentalCompany = :companyName AND"
                + "                             t.companyName = :companyName"
                + "GROUP BY t"
    )
})
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
    
    public Car getCarId() {
    	return car;
    }
    
    /*************
     * TO STRING *
     *************/
    
    @Override
    public String toString() {
        return String.format("Reservation for %s from %s to %s at %s\nCar type: %s\tCar: %s\nTotal price: %.2f", 
                getCarRenter(), getStartDate(), getEndDate(), getRentalCompany(), getCarType(), getCarId(), getRentalPrice());
    }	

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}