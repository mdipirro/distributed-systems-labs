package session;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateful;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Quote;
import rental.RentalStore;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

@Stateful
public class CarRentalSession implements CarRentalSessionRemote {
    
    private List<Quote> quotes;
    
    public CarRentalSession() {
        quotes = new ArrayList<Quote>();
    }

    @Override
    public Set<String> getAllRentalCompanies() {
        return new HashSet<String>(RentalStore.getRentals().keySet());
    }

    @Override
    public Quote createQuote(ReservationConstraints constraints, String guest)
    throws ReservationException {
        boolean companyFound = false;
        Iterator<CarRentalCompany> iterator = RentalStore.getRentals().values().iterator();
        CarRentalCompany company = null;
        // Look for a company having the requested region 
        while (iterator.hasNext() && !companyFound) {
            company = iterator.next();
            companyFound = (company.hasRegion(constraints.getRegion()));
        }
        if (company != null) { // A company has been found
            Quote quote = company.createQuote(constraints, guest);
            quotes.add(quote);
            return quote;
        }
        // No companies with the requested region, throw an exception
        throw new ReservationException("No available company with the "
                + "requested region <" + constraints.getRegion() + ">");
    }

    @Override
    public List<Reservation> confirmQuotes() throws ReservationException {
        // Allocate a list of reservation with the same siza as quotes
        List<Reservation> reservations = new ArrayList<Reservation>(quotes.size());
        Quote failed = null;
        try {
            // For each quote, try to confirm it
            for (Quote quote : quotes) {
                failed = quote;
                reservations.add(
                    RentalStore.getRental(quote.getRentalCompany()).confirmQuote(quote)
                );
            }
            return reservations;
        } catch (ReservationException exc) {
            for (Reservation reservation : reservations) {
                RentalStore.getRental(reservation.getRentalCompany()).cancelReservation(reservation);
            }
            quotes.remove(failed);
            throw exc;
        }
    }

    @Override
    public List<Quote> getCurrentQuotes() {
        return quotes;
    }
}
