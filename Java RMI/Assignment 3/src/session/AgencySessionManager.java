package session;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import namingservice.NamingServiceRemote;

public class AgencySessionManager implements AgencySessionManagerRemote {

    private final Map<String, RentalSessionRemote> rentalSessions;
    private NamingServiceRemote namingService;

    public AgencySessionManager(NamingServiceRemote namingService) {
        this.rentalSessions = new HashMap<>();
        this.namingService = namingService;
    }

    @Override
    public synchronized RentalSessionRemote getRentalSession(String clientName)
            throws RemoteException, IllegalArgumentException {
        if (clientName == null) {
            throw new IllegalArgumentException("Client's name must be non null!");
        }
        RentalSessionRemote stub = rentalSessions.get(clientName);
        if(stub == null){
            RentalSession session = new RentalSession(clientName, namingService);
            stub = (RentalSessionRemote) UnicastRemoteObject.exportObject(session, 0);
            rentalSessions.put(clientName, stub);
        }
        return stub;
    }

    @Override
    public synchronized void terminateRentalSession(String clientName) throws RemoteException {
        rentalSessions.remove(clientName);
    }
}
