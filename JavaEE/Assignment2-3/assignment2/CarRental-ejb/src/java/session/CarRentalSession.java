package session;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import static javax.ejb.TransactionAttributeType.REQUIRED;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Quote;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

@Stateful
public class CarRentalSession implements CarRentalSessionRemote {
    
    @PersistenceContext
    private EntityManager em;
    
    @Resource
    private EJBContext context;

    private String renter;
    private List<Quote> quotes = new LinkedList<>();

    @Override
    public Set<String> getAllRentalCompanies() {
        return new HashSet(em.createNamedQuery("findAllRentalCompanies").getResultList());
    }
    
    @Override
    public List<CarType> getAvailableCarTypes(Date start, Date end) {
        return em.createNamedQuery("getAvailableCarTypes")
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }

    @Override
    public Quote createQuote(ReservationConstraints constraints) throws ReservationException { 
        List<CarRentalCompany> companies = em.createNamedQuery("getAvailableCarRentalCompanies")
                .setParameter("start", constraints.getStartDate())
                .setParameter("end", constraints.getEndDate())
                .setParameter("region", constraints.getRegion())
                .setParameter("carType", constraints.getCarType())
                .getResultList();
        if (!companies.isEmpty()) { // A company has been found 
            Quote quote = companies.get(0).createQuote(constraints, renter); 
            quotes.add(quote); 
            return quote; 
        } 
        // No companies with the requested region, throw an exception 
        throw new ReservationException("No available company with the " 
                + "requested constraints"); 
    } 


    @Override
    public List<Quote> getCurrentQuotes() {
        return quotes;
    }

    @TransactionAttribute(REQUIRED)
    @Override
    public List<Reservation> confirmQuotes() throws ReservationException {
        List<Reservation> done = new LinkedList<Reservation>();
        try {
            for (Quote quote : quotes) {
                done.add(em.find(CarRentalCompany.class, quote.getRentalCompany()).confirmQuote(quote));
            }
        } catch (Exception e) {
            context.setRollbackOnly();
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
    public String getCheapestCarTypes(Date start, Date end, String region){ 
        List<CarType> types = em.createNamedQuery("findCheapestCarType")
                .setParameter("start", start)
                .setParameter("end", end)
                .setParameter("region", region)
                .getResultList();
        return types.get(0).getName();
    }
}