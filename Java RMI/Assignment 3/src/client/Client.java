package client;

import configuration.AgencyConfigRMI;
import rental.CarType;
import rental.Reservation;
import rental.ReservationConstraints;
import session.AgencySessionManagerRemote;
import session.ManagerSessionRemote;
import session.RentalSessionRemote;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class Client extends AbstractTestManagement<RentalSessionRemote, ManagerSessionRemote>{

    private AgencySessionManagerRemote agencySessionManager;
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
            agencySessionManager = (AgencySessionManagerRemote)
                    registry.lookup(AgencyConfigRMI.AGENCY_SESSION_MANAGER_NAME);
            managerSession = (ManagerSessionRemote)
                    registry.lookup(AgencyConfigRMI.MANAGER_SESSION);
        } catch (Exception e) {
            System.err.println("EXCEPTION during client creation:");
            e.printStackTrace();
        }
    }

    @Override
    protected Set<String> getBestClients(ManagerSessionRemote ms) throws Exception {
        return ms.getBestCostumers();
    }

    @Override
    protected String getCheapestCarType(RentalSessionRemote o, Date start, Date end, String region) throws Exception {
        return o.getCheapestCarType(start, end, region).getName();
    }

    @Override
    protected CarType getMostPopularCarTypeIn(ManagerSessionRemote ms, String carRentalCompanyName, int year) throws Exception {
        return ms.getMostPopularCarType(carRentalCompanyName, year);
    }

    @Override
    protected String getMostPopularCarRentalCompany(ManagerSessionRemote ms) throws Exception {
        return ms.getMostPopularCarRentalCompany();
    }

    @Override
    protected RentalSessionRemote getNewReservationSession(String name) throws Exception {
        return agencySessionManager.getRentalSession(name);
    }

    @Override
    protected ManagerSessionRemote getNewManagerSession(String name, String carRentalName) throws Exception {
        return managerSession;
    }

    @Override
    protected void checkForAvailableCarTypes(RentalSessionRemote o, Date start, Date end) throws Exception {
        for(CarType type : o.getAvailableCarTypes(start, end)){
            System.out.println(type);
        }
    }

    @Override
    protected void addQuoteToSession(RentalSessionRemote o, String name, Date start, Date end, String carType, String region) throws Exception {
        o.createQuote(new ReservationConstraints(start, end, carType, region));
    }

    @Override
    protected List<Reservation> confirmQuotes(RentalSessionRemote o, String name) throws Exception {
        return o.confirmQuotes();
    }

    @Override
    protected int getNumberOfReservationsForCarType(ManagerSessionRemote ms, String carRentalName, String carType) throws Exception {
        return ms.getNumberOfReservations(carType, carRentalName);
    }
}
