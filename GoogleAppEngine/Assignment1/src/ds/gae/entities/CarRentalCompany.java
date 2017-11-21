package ds.gae.entities;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import static javax.persistence.CascadeType.DETACH;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REFRESH;
import ds.gae.ReservationException;

@Entity
@Table(name = "COMPANY")
@NamedQueries ({
	/*@NamedQuery(
		name = "getCarTypesNamesByCompany",
		query = "SELECT company.carTypes "
				+ "FROM CarRentalCompany company "
				+ "WHERE company.name = :companyName"
	),*/
	@NamedQuery(
		name = "getCarTypesByCompany",
		query = "SELECT company.carTypes "
				+ "FROM CarRentalCompany company "
				+ "WHERE company.name = :companyName"
	),
	@NamedQuery(
		name = "getCarRentalCompanyNames",
		query = "SELECT company.name "
				+ "FROM CarRentalCompany company "
	),
	@NamedQuery(
		name = "getReservationsByCarRenter",
		query = "SELECT reservation "
				+ "FROM Reservation reservation "
				+ "WHERE reservation.carRenter = :renter"
	)/*,
	@NamedQuery(
		name = "getCarIdsByCarType",
		query = "SELECT car.id "
				+ "FROM CarRentalCompany company, IN(company.carTypes) carType, IN(type.cars) car "
				+ "WHERE company.name = :companyName AND "
				+ "		 carType.name = :typeName"
	),
	@NamedQuery(
		name = "getCarsByCarType",
		query = "SELECT car "
				+ "FROM CarRentalCompany company, IN(company.carTypes) carType, IN(type.cars) car "
				+ "WHERE company.name = :companyName AND "
				+ "		 carType.name = :typeName"
	)*/
})
public class CarRentalCompany implements Serializable {

	private static Logger logger = Logger.getLogger(CarRentalCompany.class.getName());
	
	@Id
	private String name;
	
	//@OneToMany(cascade = ALL)
	//private Set<Car> cars;
	
	@OneToMany(cascade = {CascadeType.REMOVE,CascadeType.PERSIST})
	//private Set<CarType> carTypes = new HashSet<CarType>();
	private Map<String,CarType> carTypes = new HashMap<String, CarType>();

	/***************
	 * CONSTRUCTOR *
	 ***************/

	public CarRentalCompany(String name, Set<CarType> types) {
		logger.log(Level.INFO, "<{0}> Car Rental Company {0} starting up...", name);
		setName(name);
		//this.cars = cars;
		for(CarType carType : types)
			carTypes.put(carType.getName(), carType);
			//carTypes.add(carType);
	}
	
	public CarRentalCompany(String name) {
		this.name = name;
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

	/*************
	 * CAR TYPES *
	 *************/

	public Collection<CarType> getAllCarTypes() {
		return carTypes.values();
		//return carTypes;
	}
	
	public CarType getCarType(String carTypeName) {
		for (CarType type : carTypes.values()) {
			if (type.getName().equals(carTypeName)) {
				return type;
			}
		}
		throw new IllegalArgumentException("<" + carTypeName + "> No car type of name " + carTypeName);
	}
	
	public boolean isAvailable(String carTypeName, Date start, Date end) {
		logger.log(Level.INFO, "<{0}> Checking availability for car type {1}", new Object[]{name, carTypeName});
		for (CarType type : carTypes.values()) {
			if (type.getName().equals(carTypeName)) {
				return getAvailableCarTypes(start, end).contains(type);
			}
		}
		throw new IllegalArgumentException("<" + carTypeName + "> No car type of name " + carTypeName);
	}
	
	public Set<CarType> getAvailableCarTypes(Date start, Date end) {
		Set<CarType> availableCarTypes = new HashSet<CarType>();
		for (CarType type : carTypes.values()) {
			for (Car car : type.getCars()) {
				if (car.isAvailable(start, end)) {
					availableCarTypes.add(type);
				}
			}
		}
		return availableCarTypes;
	}
	
	/*********
	 * CARS *
	 *********/
	
	private Car getCar(int uid) {
		for (CarType type : carTypes.values()) {
			for (Car car : type.getCars()) {
				if (car.getId() == uid)
					return car;
			}
		}
		throw new IllegalArgumentException("<" + name + "> No car with uid " + uid);
	}
	
	public Set<Car> getCars() {
		Set<Car> result = new HashSet<Car>();
		for (CarType type : carTypes.values()) {
			result.addAll(type.getCars());
		}
    	return result;
    }
	
	private List<Car> getAvailableCars(String carType, Date start, Date end) {
		List<Car> availableCars = new LinkedList<Car>();
		for (CarType type : carTypes.values()) {
			for (Car car : type.getCars()) {
				if (type.getName().equals(carType) && car.isAvailable(start, end)) {
					availableCars.add(car);
				}
			}
		}
		return availableCars;
	}

	/****************
	 * RESERVATIONS *
	 ****************/

	public Quote createQuote(ReservationConstraints constraints, String client)
			throws ReservationException {
		logger.log(Level.INFO, "<{0}> Creating tentative reservation for {1} with constraints {2}", 
                        new Object[]{name, client, constraints.toString()});
		
		CarType type = getCarType(constraints.getCarType());
		
		if(!isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate()))
			throw new ReservationException("<" + name
				+ "> No cars available to satisfy the given constraints.");
		
		double price = calculateRentalPrice(type.getRentalPricePerDay(),constraints.getStartDate(), constraints.getEndDate());
		
		return new Quote(client, constraints.getStartDate(), constraints.getEndDate(), getName(), constraints.getCarType(), price);
	}

	// Implementation can be subject to different pricing strategies
	private double calculateRentalPrice(double rentalPricePerDay, Date start, Date end) {
		return rentalPricePerDay * Math.ceil((end.getTime() - start.getTime())
						/ (1000 * 60 * 60 * 24D));
	}

	public Reservation confirmQuote(Quote quote) throws ReservationException {
		logger.log(Level.INFO, "<{0}> Reservation of {1}", new Object[]{name, quote.toString()});
		List<Car> availableCars = getAvailableCars(quote.getCarType(), quote.getStartDate(), quote.getEndDate());
		if(availableCars.isEmpty())
			throw new ReservationException("Reservation failed, all cars of type " + quote.getCarType()
	                + " are unavailable from " + quote.getStartDate() + " to " + quote.getEndDate());
		Car car = availableCars.get((int)(Math.random()*availableCars.size()));
		
		Reservation res = new Reservation(quote, car.getId());
		car.addReservation(res);
		return res;
	}

	public void cancelReservation(Reservation res) {
		logger.log(Level.INFO, "<{0}> Cancelling reservation {1}", new Object[]{name, res.toString()});
		getCar(res.getCarId()).removeReservation(res);
	}
}