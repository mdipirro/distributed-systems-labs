package client;

import rental.CarType;
import rental.Reservation;
import rental.ReservationConstraints;
import server.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class Client extends AbstractTestManagement {

    private RentalSessionManagerRemote rentalSessionManager;
    private ManagerSessionRemote managerSession;

    public static void main(String[] args) throws Exception {
        Client main = new Client("trips");
        main.run();
    }

    public Client(String scriptFile) {
        super(scriptFile);
        Registry registry;
        try {
            System.setSecurityManager(null);
            registry = LocateRegistry.getRegistry();
            rentalSessionManager = (RentalSessionManagerRemote)
                    registry.lookup(RentalServer.RENTAL_SESSION_MANAGER_NAME);
            managerSession = (ManagerSessionRemote)
                    registry.lookup(RentalServer.MANAGER_SESSION_NAME);
        } catch (Exception e) {
            System.err.println("EXCEPTION during client creation:");
            e.printStackTrace();
        }
    }

    @Override
    protected Set<String> getBestClients(Object ms) throws Exception {
        return ((ManagerSessionRemote)ms).getBestCostumer();
    }

    @Override
    protected String getCheapestCarType(Object o, Date start, Date end, String region) throws Exception {
        return ((RentalSessionRemote)o).getCheapestCarType(start, end, region).getName();
    }

    @Override
    protected CarType getMostPopularCarTypeIn(Object ms, String carRentalCompanyName, int year) throws Exception {
        return ((ManagerSessionRemote)ms).getMostPopularCarType(carRentalCompanyName, year);
    }

    @Override
    protected Object getNewReservationSession(String name) throws Exception {
        return rentalSessionManager.add(name);
    }

    @Override
    protected Object getNewManagerSession(String name, String carRentalName) throws Exception {
        return managerSession;
    }

    @Override
    protected void checkForAvailableCarTypes(Object o, Date start, Date end) throws Exception {
        ((RentalSessionRemote)o).getAvailableCarTypes(start, end);
    }

    @Override
    protected void addQuoteToSession(Object o, String name, Date start, Date end, String carType, String region) throws Exception {
        ((RentalSessionRemote)o).createQuote(new ReservationConstraints(
                start, end, carType, region
        ));
    }

    @Override
    protected List<Reservation> confirmQuotes(Object o, String name) throws Exception {
        return ((RentalSessionRemote)o).confirmQuotes();
    }

    @Override
    protected int getNumberOfReservationsForCarType(Object ms, String carRentalName, String carType) throws Exception {
        return ((ManagerSessionRemote)ms).getNumberOfReservations(carType, carRentalName);
    }
}
