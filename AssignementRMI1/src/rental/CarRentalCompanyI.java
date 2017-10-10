package rental;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface CarRentalCompanyI extends Remote {
	Set<CarType> getAvailableCarTypes(Date start, Date end) throws RemoteException;
	List<String> getRegions() throws RemoteException;
	Quote createQuote(ReservationConstraints constraints, String client)
			throws ReservationException, RemoteException;
    Reservation confirmQuote(Quote quote) throws ReservationException, RemoteException;
	List<Reservation> getReservationsByRenter(String renter) throws RemoteException;
	List<Reservation> getReservationsByCarType(String carType) throws RemoteException;
}
