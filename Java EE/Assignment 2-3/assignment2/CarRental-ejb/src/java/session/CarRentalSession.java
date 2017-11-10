package session;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Quote;
import rental.RentalStore;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

@Stateful
public class CarRentalSession implements CarRentalSessionRemote {
    
    @PersistenceContext
    private EntityManager em;

    private String renter;
    private List<Quote> quotes = new LinkedList<Quote>();

    @Override
    public Set<String> getAllRentalCompanies() {
        return new HashSet(em.createNamedQuery("findAllRentalCompanies").getResultList());
    }
    
    @Override
    public List<CarType> getAvailableCarTypes(Date start, Date end) {
        /*List<CarType> availableCarTypes = new LinkedList<CarType>();
        for(String crc : getAllRentalCompanies()) {
            for(CarType ct : RentalStore.getRentals().get(crc).getAvailableCarTypes(start, end)) {
                if(!availableCarTypes.contains(ct))
                    availableCarTypes.add(ct);
            }
        }
        return availableCarTypes;*/
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Quote createQuote(ReservationConstraints constraints) throws ReservationException { 
        boolean companyFound = false; 
        Iterator<CarRentalCompany> iterator = RentalStore.getRentals().values().iterator(); 
        CarRentalCompany company = null; 
        // Look for a company having the requested region  
        while (iterator.hasNext() && !companyFound) { 
            company = iterator.next(); 
            companyFound = (company.hasRegion(constraints.getRegion())) && 
                    company.isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate()); 
        } 
        if (company != null) { // A company has been found 
            Quote quote = company.createQuote(constraints, renter); 
            quotes.add(quote); 
            return quote; 
        } 
        // No companies with the requested region, throw an exception 
        throw new ReservationException("No available company with the " 
                + "requested region <" + constraints.getRegion() + ">"); 
    } 


    @Override
    public List<Quote> getCurrentQuotes() {
        return quotes;
    }

    @Override
    public List<Reservation> confirmQuotes() throws ReservationException {
        List<Reservation> done = new LinkedList<Reservation>();
        try {
            for (Quote quote : quotes) {
                done.add(RentalStore.getRental(quote.getRentalCompany()).confirmQuote(quote));
            }
        } catch (Exception e) {
            for(Reservation r:done)
                RentalStore.getRental(r.getRentalCompany()).cancelReservation(r);
            throw new ReservationException(e);
        }
        return done;
    }

    @Override
    public void setRenterName(String name) {
        if (renter != null) {
            throw new IllegalStateException("name already set");
        }
        renter = name;
    }
    
    @Override 
    public String getCheapestCarTypes(Date start, Date end, String region) { 
        /*return ((CarType)em.createNamedQuery("findCheapestCar")
                .setParameter("start", start)
                .setParameter("end", end)
                .setParameter("region", region)
                .getSingleResult()).getName();*/
        throw new UnsupportedOperationException("Not supported yet."); 
    } 
}