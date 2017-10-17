package session;

import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.ejb.Remote;
import rental.CarType;
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
    List<Reservation> confirmQuotes() throws ReservationException;
    
}
