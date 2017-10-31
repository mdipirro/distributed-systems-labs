package rental;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface CarRentalCompanyRemote extends Remote {
    Set<CarType> getAvailableCarTypes(Date start, Date end) throws RemoteException;
    Quote createQuote(ReservationConstraints constraints, String client) throws ReservationException, RemoteException;
    Reservation confirmQuote(Quote quote) throws ReservationException, RemoteException;
    String getName() throws RemoteException;
    boolean isAvailable(String carTypeName, Date start, Date end) throws RemoteException;
    boolean hasRegion(String region) throws RemoteException;
    boolean hasCarType(String carType) throws RemoteException;
    void cancelReservation(Reservation res) throws RemoteException;
    Collection<CarType> getAllCarTypes() throws RemoteException;
    double calculateRentalPrice(double rentalPricePerDay, Date start, Date end) throws RemoteException;
    List<Car> getCars() throws RemoteException;
    List<Reservation> getReservationsByRenter(String renter) throws RemoteException;
    int getNumberOfReservationsForCarType(String carType) throws RemoteException;
}
