package rental;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.CascadeType.DETACH;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REFRESH;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "COMPANY")
@NamedQueries({
    @NamedQuery(
        name = "findAllRentalCompanies",
        query = "SELECT company.name FROM CarRentalCompany company"
    ),
     @NamedQuery(
        name = "findRentalCompanyByName",
        query = "SELECT company FROM CarRentalCompany company WHERE company.name = :companyName"
    ),
    @NamedQuery(
        name = "getAvailableCarRentalCompanies",
        query = "SELECT company "
                + "FROM CarRentalCompany company JOIN Car car "
                + "WHERE :region MEMBER OF company.regions AND "
                + "      car IN(company.cars) AND "
                + "      car.id NOT IN (" 
                + "         SELECT res.carId "
                + "         FROM Reservation res "
                + "         WHERE (res.startDate <= :start AND res.endDate >= :start) OR "
                + "               (res.startDate <= :end AND res.endDate >= :end) "
                + "      ) AND "
                + "      :carType IN ("
                + "         SELECT t.name"
                + "         FROM CarRentalCompany comp, IN(comp.carTypes) t "
                + "         WHERE comp.name = company.name"
                + "      )"
    ),
    @NamedQuery(
        name = "getCarTypes",
        query = "SELECT company.carTypes "
                + "FROM CarRentalCompany company "
                + "WHERE company.name = :companyName"
    ),
    @NamedQuery(
        name = "getCarTypeByName",
        query = "SELECT carType "
                + "FROM CarRentalCompany company, IN(company.carTypes) carType "
                + "WHERE company.name = :companyName AND"
                + "      carType.name = :typeName"
    ),
    @NamedQuery(
        name = "getCarIds",
        query = "SELECT car.id "
                + "FROM CarRentalCompany company, IN(company.cars) car "
                + "WHERE company.name = :companyName AND "
                + "      car.type = :carType"    
    ),
    @NamedQuery(
        name = "findCarTypesByCompany",
        query = "SELECT t "
                + "FROM CarRentalCompany company JOIN CarType t "
                + "WHERE company.name = :companyName AND "
                + "      t IN(company.carTypes)"
    ),
    @NamedQuery(
        name = "getNumberOfReservationsByCar",
        query = "SELECT COUNT(car.reservations) "
                + "FROM CarRentalCompany company JOIN Car car "
                + "WHERE company.name = :companyName AND"
                + "      car.id = :carId AND "
                + "      car IN(company.cars)"
    ),
    @NamedQuery(
        name = "getNumberOfReservationByCarType",
        query = "SELECT COUNT(car.reservations) "
                + "FROM CarRentalCompany company JOIN Car car "
                + "WHERE car.type.name = :carType "
                + "      AND company.name = :companyName "
                + "      AND car IN(company.cars)"
    ),
    @NamedQuery(
        name = "findCheapestCarType",
        query = "SELECT car.type "
                + "FROM CarRentalCompany company JOIN Car car "
                + "WHERE :region MEMBER OF company.regions AND " // region constraint
                + "car IN(company.cars) AND " 
                + "car.id NOT IN (" // availability
                + " SELECT res.carId "
                + " FROM Reservation res "
                + " WHERE (res.startDate <= :start AND res.endDate >= :start) OR "
                + "        (res.startDate <= :end AND res.endDate >= :end) "
                + ") " 
                + "ORDER BY car.type.rentalPricePerDay asc"
    )
})
public class CarRentalCompany {

    private static Logger logger = Logger.getLogger(CarRentalCompany.class.getName());
    @Id 
    private String name;
    
    @OneToMany(cascade = ALL, mappedBy = "company")
    private List<Car> cars;
    
    @OneToMany(cascade = {PERSIST, MERGE, REFRESH, DETACH}, fetch = FetchType.LAZY)
    private Set<CarType> carTypes = new HashSet<CarType>();
    @ElementCollection
    private List<String> regions;

	
    /***************
     * CONSTRUCTOR *
     ***************/

    public CarRentalCompany() {}
        
    public CarRentalCompany(String name, List<String> regions, List<Car> cars) {
        logger.log(Level.INFO, "<{0}> Car Rental Company {0} starting up...", name);
        setName(name);
        this.cars = cars;
        setRegions(regions);
        for (Car car : cars) {
            carTypes.add(car.getType());
        }
    }
    
    public Boolean hasRegion(String region){ 
        return this.regions.contains(region); 
    }

    /********
     * NAME *
     ********/
    
    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    /***********
     * Regions *
     **********/
    private void setRegions(List<String> regions) {
        this.regions = regions;
    }
    
    public List<String> getRegions() {
        return this.regions;
    }

    /*************
     * CAR TYPES *
     *************/
    public Collection<CarType> getAllTypes() {
        return carTypes;
    }

    public CarType getType(String carTypeName) {
        for(CarType type:carTypes){
            if(type.getName().equals(carTypeName))
                return type;
        }
        return null;
        //throw new IllegalArgumentException("<" + carTypeName + "> No cartype of name " + carTypeName);  // TODO should this throw exception?
    }

    public boolean isAvailable(String carTypeName, Date start, Date end) {
        logger.log(Level.INFO, "<{0}> Checking availability for car type {1}", new Object[]{name, carTypeName});
        return getAvailableCarTypes(start, end).contains(getType(carTypeName));
    }    

    public Set<CarType> getAvailableCarTypes(Date start, Date end) {
        Set<CarType> availableCarTypes = new HashSet<CarType>();
        for (Car car : cars) {
            if (car.isAvailable(start, end)) {
                availableCarTypes.add(car.getType());
            }
        }
        return availableCarTypes;
    }
    
    public void addCarType(CarType carType) {
        carTypes.add(carType);
    }

    /*********
     * CARS *
     *********/
    public Car getCar(int uid) {
        for (Car car : cars) {
            if (car.getId() == uid) {
                return car;
            }
        }
        throw new IllegalArgumentException("<" + name + "> No car with uid " + uid);
    }

    public Set<Car> getCars(CarType type) {
        Set<Car> out = new HashSet<Car>();
        for (Car car : cars) {
            if (car.getType().equals(type)) {
                out.add(car);
            }
        }
        return out;
    }
    
    public Set<Car> getCars(String type) {
        Set<Car> out = new HashSet<Car>();
        for (Car car : cars) {
            if (type.equals(car.getType().getName())) {
                out.add(car);
            }
        }
        return out;
    }

    private List<Car> getAvailableCars(String carType, Date start, Date end) {
        List<Car> availableCars = new LinkedList<Car>();
        for (Car car : cars) {
            if (car.getType().getName().equals(carType) && car.isAvailable(start, end)) {
                availableCars.add(car);
            }
        }
        return availableCars;
    }

    public void addCar(Car car) {
        cars.add(car);
    }

    /****************
     * RESERVATIONS *
     ****************/
    
    public Quote createQuote(ReservationConstraints constraints, String guest)
            throws ReservationException {
        logger.log(Level.INFO, "<{0}> Creating tentative reservation for {1} with constraints {2}",
                new Object[]{name, guest, constraints.toString()});


        if (!this.regions.contains(constraints.getRegion()) || !isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate())) {
            throw new ReservationException("<" + name
                    + "> No cars available to satisfy the given constraints.");
        }
		
        CarType type = getType(constraints.getCarType());

        double price = calculateRentalPrice(type.getRentalPricePerDay(), constraints.getStartDate(), constraints.getEndDate());

        return new Quote(guest, constraints.getStartDate(), constraints.getEndDate(), getName(), constraints.getCarType(), price);
    }

    // Implementation can be subject to different pricing strategies
    private double calculateRentalPrice(double rentalPricePerDay, Date start, Date end) {
        return rentalPricePerDay * Math.ceil((end.getTime() - start.getTime())
                / (1000 * 60 * 60 * 24D));
    }

    public Reservation confirmQuote(Quote quote) throws ReservationException {
        logger.log(Level.INFO, "<{0}> Reservation of {1}", new Object[]{name, quote.toString()});
        List<Car> availableCars = getAvailableCars(quote.getCarType(), quote.getStartDate(), quote.getEndDate());
        if (availableCars.isEmpty()) {
            throw new ReservationException("Reservation failed, all cars of type " + quote.getCarType()
                    + " are unavailable from " + quote.getStartDate() + " to " + quote.getEndDate());
        }
        Car car = availableCars.get((int) (Math.random() * availableCars.size()));

        Reservation res = new Reservation(quote, car.getId());
        car.addReservation(res);
        return res;
    }
    
    public void cancelReservation(Reservation res) {
        logger.log(Level.INFO, "<{0}> Cancelling reservation {1}", new Object[]{name, res.toString()});
        getCar(res.getCarId()).removeReservation(res);
    }
    
    public Set<Reservation> getReservationsBy(String renter) {
        logger.log(Level.INFO, "<{0}> Retrieving reservations by {1}", new Object[]{name, renter});
        Set<Reservation> out = new HashSet<Reservation>();
        for(Car c : cars) {
            for(Reservation r : c.getReservations()) {
                if(r.getCarRenter().equals(renter))
                    out.add(r);
            }
        }
        return out;
    }

    public List<Car> getCars() {
        return cars;
    }

    public Set<CarType> getCarTypes() {
        return carTypes;
    }
    
}