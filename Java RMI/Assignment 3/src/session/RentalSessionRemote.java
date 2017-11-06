package session;

import rental.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface RentalSessionRemote extends Remote {
    Quote createQuote(ReservationConstraints constraints)
            throws ReservationException, RemoteException;
    List<Quote> getCurrentQuotes() throws RemoteException;
    List<Reservation> confirmQuotes() throws ReservationException, RemoteException;
    List<CarType> getAvailableCarTypes(Date start, Date end) throws RemoteException;
    CarType getCheapestCarType(Date start, Date end, String region) throws RemoteException;
    void removeQuotes() throws RemoteException;
}
