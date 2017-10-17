package session;

import java.util.List;
import java.util.Set;
import javax.ejb.Remote;
import rental.Quote;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

@Remote
public interface CarRentalSessionRemote {

    Set<String> getAllRentalCompanies();
    Quote createQuote(ReservationConstraints constraints, String guest)
            throws ReservationException;
    List<Quote> getCurrentQuotes();
    Reservation confirmQuote(Quote quote) throws ReservationException;
    
}
