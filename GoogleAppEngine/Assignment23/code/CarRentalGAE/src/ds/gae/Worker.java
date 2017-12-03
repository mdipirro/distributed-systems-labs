package ds.gae;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ds.gae.entities.CarRentalCompany;
import ds.gae.entities.Quote;
import ds.gae.entities.QuotesStatus;
import ds.gae.entities.Reservation;

public class Worker extends HttpServlet {
	private static final long serialVersionUID = -7058685883212377590L;
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		EntityManager em = EMF.get().createEntityManager();
		ObjectInputStream ois = new ObjectInputStream(req.getInputStream());
		QuotesStatus status = null;
		try {
			Payload payload = (Payload) ois.readObject();
			status = em.find(QuotesStatus.class, payload.getID());
			if (status != null) {
				status.setStatus(QuotesStatus.Status.IN_PROCESSING);
				confirmQuotes(payload.getQuotes()); // if throws an exception, set status to FAILED
				status.setStatus(QuotesStatus.Status.CONFIRMED);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (ReservationException e) {
			status.setStatus(QuotesStatus.Status.FAILED);
		} finally {
			em.close();
			ois.close();
		}
	}
	
	public List<Reservation> confirmQuotes(List<Quote> quotes) throws ReservationException { 
    	List<Reservation> reservations = new ArrayList<Reservation>();	
    	Map<String, List<Quote>> quotesForSingleCRC = new HashMap<>(); 
    	EntityManager em = EMF.get().createEntityManager(); 
		try{
			for(Quote q: quotes){
				if(!quotesForSingleCRC.containsKey(q.getRentalCompany()))
					quotesForSingleCRC.put(q.getRentalCompany(),new ArrayList<Quote>());
				quotesForSingleCRC.get(q.getRentalCompany()).add(q);
			}
			for(List<Quote> listOfQuotes : quotesForSingleCRC.values())
				reservations.addAll(confirmQuotesForConcreteCompany(listOfQuotes));
		}catch(Exception e) {
			for(Reservation res : reservations){
				CarRentalCompany company = em.find(CarRentalCompany.class, res.getRentalCompany());
				company.cancelReservation(res);
			}
			reservations.clear();	
			throw new ReservationException(e.toString());
		}
		finally{
			em.close();
		}
		return reservations;
    }
	
	private List<Reservation> confirmQuotesForConcreteCompany(List<Quote> quotes) throws ReservationException{
    	List<Reservation> reservations = new ArrayList<Reservation>();	
    	EntityManager em = EMF.get().createEntityManager(); 
    	EntityTransaction t = em.getTransaction();
    	try{
    		t.begin();
			for(Quote q: quotes){
				CarRentalCompany company = em.find(CarRentalCompany.class, q.getRentalCompany());
				reservations.add(company.confirmQuote(q));
			}
			t.commit();
			System.out.println(quotes.size()+" quotes for company: "+quotes.get(0).getRentalCompany()+" were confirmed");
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
}
