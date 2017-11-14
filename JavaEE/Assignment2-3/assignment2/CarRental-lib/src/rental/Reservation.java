package rental;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "RESERVATION")
@NamedQueries({
    @NamedQuery(
        name = "findBestCostumers",
        query = "SELECT reservation.carRenter "
            + "FROM Reservation reservation "
        + "WHERE ("
            + " SELECT COUNT(res.id) " // how many reservations for this renter?
            + " FROM Reservation res "
            + " WHERE res.carRenter = reservation.carRenter "
            + ") =  :maxCount"
    ),
    @NamedQuery(
        name = "findMostPopularCarType",
        query = "SELECT res.carType "
                + "FROM Reservation res "
                + "WHERE res.rentalCompany = :companyName AND "
                + "      FUNC('YEAR', res.startDate) = :year "
                + "GROUP BY res.carType "
                + "ORDER BY COUNT(res.id) desc"
    ),
    @NamedQuery(
        name = "findMaxReservationCount",
        query = "SELECT COUNT(res.id) " +
            "FROM Reservation res " +
            "GROUP BY res.carRenter " +
            "ORDER BY COUNT(res.id) DESC"
    )
})
public class Reservation extends Quote {

    private static final long serialVersionUID = 7888890731888905023L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    private int carId;
    
    /***************
     * CONSTRUCTOR *
     ***************/
    public Reservation() {
        super();
    }
    
    public Reservation(Quote quote, int carId) {
    	super(quote.getCarRenter(), quote.getStartDate(), quote.getEndDate(), 
    		quote.getRentalCompany(), quote.getCarType(), quote.getRentalPrice());
        this.carId = carId;
    }
    
    /******
     * ID *
     ******/
    
    public int getCarId() {
    	return carId;
    }
    
    /*************
     * TO STRING *
     *************/
    
    @Override
    public String toString() {
        return String.format("Reservation for %s from %s to %s at %s\nCar type: %s\tCar: %s\nTotal price: %.2f", 
                getCarRenter(), getStartDate(), getEndDate(), getRentalCompany(), getCarType(), carId, getRentalPrice());
    }
}