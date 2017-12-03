package ds.gae;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import ds.gae.entities.Car;
import ds.gae.entities.CarRentalCompany;
import ds.gae.entities.CarType;
import ds.gae.entities.Quote;
import ds.gae.entities.QuotesStatus;
import ds.gae.entities.Reservation;
import ds.gae.entities.ReservationConstraints;

public class CarRentalModel {
		
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
	public List<String> getCarTypesNames(String crcName) {
		EntityManager em = EMF.get().createEntityManager();
		try {
			return em.createNamedQuery("getCarTypesNamesByCompany", String.class)
					.setParameter("companyName", crcName)
					.getResultList(); 
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
    		return em.createNamedQuery("getCarRentalCompanyNames",String.class).getResultList();
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
	public Reservation confirmQuote(Quote q) throws ReservationException {
		EntityManager em = EMF.get().createEntityManager();
		EntityTransaction t = em.getTransaction();
		try {
			t.begin();
			CarRentalCompany crc = em.find(CarRentalCompany.class, q.getRentalCompany());
	        Reservation res = crc.confirmQuote(q);
	        t.commit();
	        return res;
		} catch(Exception e){
			if (t.isActive()){
				t.rollback();
			}
			throw new ReservationException(e.toString());
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
	public void confirmQuotes(List<Quote> quotes) throws ReservationException {
		if (quotes != null && !quotes.isEmpty()) {
			QuotesStatus status = createQuotesStatus(quotes.get(0).getCarRenter());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(bos);
				Payload payload = new Payload(status.getId(), quotes);
				oos.writeObject(payload);
				QueueFactory.getDefaultQueue().add(TaskOptions.Builder.withUrl("/worker")
						.payload(bos.toByteArray()));
			} catch (IOException e) {
				throw new ReservationException("An unknown error has occurred during the confirmation");
			} finally {
				try {
					if (oos != null) {
						oos.close();
					}
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private QuotesStatus createQuotesStatus(String renter) {
		EntityManager em = EMF.get().createEntityManager();
		try {
			QuotesStatus status = new QuotesStatus(renter);
			em.persist(status);
			return status;
		} finally {
			em.close();
		}
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
		List<Reservation> result = em.createNamedQuery("getReservationsByCarRenter",Reservation.class)
				.setParameter("renter", renter)
				.getResultList();
		em.close();
		return result;
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
    		return em.createNamedQuery("getCarTypesByCompany", CarType.class)
					.setParameter("companyName", crcName)
					.getResultList(); 
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
    		return em.createNamedQuery("getCarIdsByCarType",Integer.class)
    				.setParameter("companyName", crcName)
    				.setParameter("typeName", carType.getName())
    				.getResultList();
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
			return em.createNamedQuery("getCarsByCompanyAndType",Car.class)
					.setParameter("companyName", crcName)
					.setParameter("carTypeName", carType.getName())
					.getResultList();
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