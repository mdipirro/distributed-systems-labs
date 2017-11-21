package ds.gae;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.google.appengine.api.datastore.Key;

import ds.gae.entities.Car;
import ds.gae.entities.CarRentalCompany;
import ds.gae.entities.CarType;
import ds.gae.entities.Quote;
import ds.gae.entities.Reservation;
import ds.gae.entities.ReservationConstraints;
 
public class CarRentalModel {
	
	public Map<String,CarRentalCompany> CRCS = new HashMap<String, CarRentalCompany>();	
	
	private static CarRentalModel instance;
	
	public static CarRentalModel get() {
		if (instance == null)
			instance = new CarRentalModel();
		return instance;
	}
		
	/**
	 * Get the car types available in the given car rental company.
	 *
	 * @param 	crcName
	 * 			the car rental company
	 * @return	The list of car types (i.e. name of car type), available
	 * 			in the given car rental company.
	 */
	public Set<String> getCarTypesNames(String crcName) {
		EntityManager em = EMF.get().createEntityManager();
		try {
			return (em.createNamedQuery("getCarTypesByCompany", Map.class)
					.setParameter("companyName", crcName)
					.getSingleResult()).keySet(); 
		} finally {
			em.close();
		}
	}

    /**
     * Get all registered car rental companies
     *
     * @return	the list return CRCS.of car rental companies
     */
    public Collection<String> getAllRentalCompanyNames() {
    	EntityManager em = EMF.get().createEntityManager();
    	try {
    		return em.createNamedQuery("getCarRentalCompanyNames").getResultList();
    	} finally {
    		em.close();
    	}
    }
	
	/**
	 * Create a quote according to the given reservation constraints (tentative reservation).
	 * 
	 * @param	company
	 * 			name of the car renter company
	 * @param	renterName 
	 * 			name of the car renter 
	 * @param 	constraints
	 * 			reservation constraints for the quote
	 * @return	The newly created quote.
	 *  
	 * @throws ReservationException
	 * 			No car available that fits the given constraints.
	 */
    public Quote createQuote(String company, String renterName, ReservationConstraints constraints) throws ReservationException {
    	EntityManager em = EMF.get().createEntityManager();
    	
    	try {
    		CarRentalCompany crc = em.find(CarRentalCompany.class, company);
        	Quote out = null;

            if (crc != null) {
                out = crc.createQuote(constraints, renterName);
            } else {
            	throw new ReservationException("CarRentalCompany not found.");    	
            }
            
            return out;
    	} finally {
    		em.close();
    	}
    }
    
	/**
	 * Confirm the given quote.
	 *
	 * @param 	q
	 * 			Quote to confirm
	 * 
	 * @throws ReservationException
	 * 			Confirmation of given quote failed.	
	 */
	public void confirmQuote(Quote q) throws ReservationException {
		EntityManager em = EMF.get().createEntityManager();
		try {
			CarRentalCompany crc = em.find(CarRentalCompany.class, q.getRentalCompany());
	        crc.confirmQuote(q);
		} finally {
			em.close();
		}	
	}
	
    /**
	 * Confirm the given list of quotes
	 * 
	 * @param 	quotes 
	 * 			the quotes to confirm
	 * @return	The list of reservations, resulting from confirming all given quotes.
	 * 
	 * @throws 	ReservationException
	 * 			One of the quotes cannot be confirmed. 
	 * 			Therefore none of the given quotes is confirmed.
	 */
    public List<Reservation> confirmQuotes(List<Quote> quotes) throws ReservationException {    	
    	List<Reservation> reservations = new ArrayList<Reservation>();	
		EntityManager em = EMF.get().createEntityManager();   	
		EntityTransaction t = em.getTransaction();
		try{
			t.begin();
			for(Quote q: quotes){
				CarRentalCompany company = em.find(CarRentalCompany.class, q.getRentalCompany());
				Reservation res = company.confirmQuote(q);
				reservations.add(res);
			}
			t.commit();
		}catch(Exception e) {
			if (t.isActive()){
				t.rollback();
			}
			throw new ReservationException(e.toString());
		}
		finally{
			em.close();
		}
		return reservations;
    }
	
	/**
	 * Get all reservations made by the given car renter.
	 *
	 * @param 	renter
	 * 			name of the car renter
	 * @return	the list of reservations of the given car renter
	 */
	public List<Reservation> getReservations(String renter) {
		EntityManager em = EMF.get().createEntityManager();
		List<Reservation> result = em.createNamedQuery("getReservationsByCarRenter")
				.setParameter("renter", renter)
				.getResultList();
		em.close();
		return result;
		//return new LinkedList<Reservation>(); //FIXME
    }

    /**
     * Get the car types available in the given car rental company.
     *
     * @param 	crcName
     * 			the given car rental company
     * @return	The list of car types in the given car rental company.
     */
    public Collection<CarType> getCarTypesOfCarRentalCompany(String crcName) {
    	EntityManager em = EMF.get().createEntityManager();
    	try {
    		return (em.createNamedQuery("getCarTypesByCompany", Map.class)
					.setParameter("companyName", crcName)
					.getSingleResult()).values(); 
    	} finally {
    		em.close();
    	}
    }
	
    /**
     * Get the list of cars of the given car type in the given car rental company.
     *
     * @param	crcName
	 * 			name of the car rental company
     * @param 	carType
     * 			the given car type
     * @return	A list of car IDs of cars with the given car type.
     */
    public Collection<Integer> getCarIdsByCarType(String crcName, CarType carType) {
    	EntityManager em = EMF.get().createEntityManager();
    	try {
    		Collection<Integer> result = new LinkedList<Integer>();
    		List<Car> cars = getCarsByCarType(crcName, carType);
    		for (Car car : cars) {
    			result.add(car.getId());
    		}
    		return result;
    	} finally {
    		em.close();
    	}
    }
    
    /**
     * Get the amount of cars of the given car type in the given car rental company.
     *
     * @param	crcName
	 * 			name of the car rental company
     * @param 	carType
     * 			the given car type
     * @return	A number, representing the amount of cars of the given car type.
     */
    public int getAmountOfCarsByCarType(String crcName, CarType carType) {
    	return this.getCarsByCarType(crcName, carType).size();
    }

	/**
	 * Get the list of cars of the given car type in the given car rental company.
	 *
	 * @param	crcName
	 * 			name of the car rental company
	 * @param 	carType
	 * 			the given car type
	 * @return	List of cars of the given car type
	 */
	private List<Car> getCarsByCarType(String crcName, CarType carType) {				
		EntityManager em = EMF.get().createEntityManager();
		try {
			Collection<CarType> carTypes = (em.createNamedQuery("getCarTypesByCompany", Map.class)
					.setParameter("companyName", crcName)
					.getSingleResult()).values();
			List<Car> result = new LinkedList<Car>();
			for (CarType type : carTypes) {
				if (type.getName().equals(carType.getName())) {
					result.addAll(type.getCars());
				}
			}
			return result;
		} finally {
			em.close();
		}
	}

	/**
	 * Check whether the given car renter has reservations.
	 *
	 * @param 	renter
	 * 			the car renter
	 * @return	True if the number of reservations of the given car renter is higher than 0.
	 * 			False otherwise.
	 */
	public boolean hasReservations(String renter) {
		return this.getReservations(renter).size() > 0;		
	}	
}