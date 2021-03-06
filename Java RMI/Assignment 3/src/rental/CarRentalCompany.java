package rental;

import configuration.AgencyConfigRMI;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
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
import session.ManagerSessionRemote;

public class CarRentalCompany implements CarRentalCompanyRemote {
    
        public static void main(String[] args) throws ReservationException, NumberFormatException, IOException {
            // for test purposes
            ManagerSessionRemote manager = null;
            System.setSecurityManager(null);
            Registry registry;
            CarRentalCompanyRemote stubRentalCompany = null;
            
            //Create new company
            List<String> regions = new ArrayList<>();  
            regions.add("Bratislava");  
            regions.add("Antwerp");  
            List<Car> cars = new ArrayList<>();  
            cars.add(new Car(999, new CarType("Skodovka", 5, (float) 1.58, 50, true)));  
            CarRentalCompany newCompany = new CarRentalCompany("Jakub", regions, cars); 
            
            try {
                registry = LocateRegistry.getRegistry();
                manager = (ManagerSessionRemote) registry.lookup(AgencyConfigRMI.MANAGER_SESSION);
                stubRentalCompany = (CarRentalCompanyRemote) UnicastRemoteObject.exportObject(newCompany, 0);
                registry.rebind(newCompany.getName(), stubRentalCompany); 
            } catch (Exception e) {
                System.err.println("EXCEPTION during CarRentalCOmpany creation:");
                e.printStackTrace();
            }
            
            //Register new companyTest
            manager.register(stubRentalCompany);  
            System.out.println("====================== Should be 3 !!! ===============================================");  
            for(CarRentalCompanyRemote company : manager.getRentals())  
                System.out.println(company.getName());
            //manager.unregister(newCompany.getName()); 
            System.out.println("====================================================================="); 
        }

	private static Logger logger = Logger.getLogger(CarRentalCompany.class.getName());
	
	private List<String> regions;
	private String name;
	private List<Car> cars;
	private Map<String,CarType> carTypes = new HashMap<String, CarType>();

	/***************
	 * CONSTRUCTOR *
	 ***************/

	public CarRentalCompany(String name, List<String> regions, List<Car> cars) {
		logger.log(Level.INFO, "<{0}> Car Rental Company {0} starting up...", name);
		setName(name);
		this.cars = cars;
		setRegions(regions);
		for(Car car:cars)
			carTypes.put(car.getType().getName(), car.getType());
		logger.log(Level.INFO, this.toString());
	}

	/********
	 * NAME *
	 ********/

	@Override
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

    @Override
    public boolean hasRegion(String region) {
        return this.regions.contains(region);
    }

    @Override
    public boolean hasCarType(String carType) {return carTypes.containsKey(carType);}
	
	/*************
	 * CAR TYPES *
	 *************/

	@Override
	public Collection<CarType> getAllCarTypes() {
		return new LinkedList<>(carTypes.values());
	}
	
	public CarType getCarType(String carTypeName) {
		if(carTypes.containsKey(carTypeName))
			return carTypes.get(carTypeName);
		throw new IllegalArgumentException("<" + carTypeName + "> No car type of name " + carTypeName);
	}
	
	// mark
    @Override
	public boolean isAvailable(String carTypeName, Date start, Date end) {
		logger.log(Level.INFO, "<{0}> Checking availability for car type {1}", new Object[]{name, carTypeName});
		if(carTypes.containsKey(carTypeName)) {
			return getAvailableCarTypes(start, end).contains(carTypes.get(carTypeName));
		} else {
			throw new IllegalArgumentException("<" + carTypeName + "> No car type of name " + carTypeName);
		}
	}

	@Override
	public Set<CarType> getAvailableCarTypes(Date start, Date end) {
		Set<CarType> availableCarTypes = new HashSet<CarType>();
		for (Car car : cars) {
			if (car.isAvailable(start, end)) {
				availableCarTypes.add(car.getType());
			}
		}
		return availableCarTypes;
	}
	
	/*********
	 * CARS *
	 *********/

	@Override
	public List<Car> getCars() {
		return cars;
	}
	
	private Car getCar(int uid) {
		for (Car car : cars) {
			if (car.getId() == uid)
				return car;
		}
		throw new IllegalArgumentException("<" + name + "> No car with uid " + uid);
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

	/****************
	 * RESERVATIONS *
	 ****************/

	public Quote createQuote(ReservationConstraints constraints, String client)
			throws ReservationException {
		logger.log(Level.INFO, "<{0}> Creating tentative reservation for {1} with constraints {2}", 
                        new Object[]{name, client, constraints.toString()});
		
				
		if(!regions.contains(constraints.getRegion()) || !isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate()))
			throw new ReservationException("<" + name
				+ "> No cars available to satisfy the given constraints.");

		CarType type = getCarType(constraints.getCarType());
		
		double price = calculateRentalPrice(type.getRentalPricePerDay(),constraints.getStartDate(), constraints.getEndDate());
		
		return new Quote(client, constraints.getStartDate(), constraints.getEndDate(), getName(), constraints.getCarType(), price);
	}

	// Implementation can be subject to different pricing strategies
    @Override
	public double calculateRentalPrice(double rentalPricePerDay, Date start, Date end) {
		return rentalPricePerDay * Math.ceil((end.getTime() - start.getTime())
						/ (1000 * 60 * 60 * 24D));
	}

	public synchronized Reservation confirmQuote(Quote quote) throws ReservationException {
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

	@Override
	public synchronized void cancelReservation(Reservation res) {
		logger.log(Level.INFO, "<{0}> Cancelling reservation {1}", new Object[]{name, res.toString()});
		getCar(res.getCarId()).removeReservation(res);
	}
	
	@Override
	public String toString() {
		return String.format("<%s> CRC is active in regions %s and serving with %d car types", name, listToString(regions), carTypes.size());
	}
	
	private static String listToString(List<? extends Object> input) {
		StringBuilder out = new StringBuilder();
		for (int i=0; i < input.size(); i++) {
			if (i == input.size()-1) {
				out.append(input.get(i).toString());
			} else {
				out.append(input.get(i).toString()+", ");
			}
		}
		return out.toString();
	}
	
	/*
	 * NEW FEATURES 
	 */
	/**
	 * Get all the reservations for a specific car renter
	 * @param renter Renter's name
	 * @return A List containing reservations for that renter
	 */
	public List<Reservation> getReservationsByRenter(String renter) {
		List<Reservation> reservations = new ArrayList<Reservation>();
		for (Car car : cars) {
			for (Reservation r : car.getReservations()) {
				if (r.getCarRenter().equals(renter)) {
					reservations.add(r);
				}
			}
		}
		return reservations;
	}
	
	/**
	 * Get the number of the reservations for a specific car type
	 * @param carType Car type
	 * @return The number of the reservations for a specific car type
	 */
	public int getNumberOfReservationsForCarType(String carType) {
		int k = 0;
		for (Car car : cars) {
			for (Reservation r : car.getReservations()) {
				if (r.getCarType().equals(carType)) {
					k++;
				}
			}
		}
		return k;
	}
}