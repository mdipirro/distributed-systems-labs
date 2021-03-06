package session;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import namingservice.NamingServiceRemote;

public class AgencySessionManager implements AgencySessionManagerRemote {

    private final Map<String, RentalSessionRemote> rentalSessions;
    private NamingServiceRemote namingService;

    public AgencySessionManager(NamingServiceRemote namingService) {
        this.rentalSessions = new ConcurrentHashMap<String, RentalSessionRemote>();
        this.namingService = namingService;
    }

    @Override
    public RentalSessionRemote getRentalSession(String clientName)
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
    public void terminateRentalSession(String clientName) throws RemoteException {
        // session will be a RentalSession's instance for sure: it is created and bound
        // in this class. The coupling remains the same
        RentalSession session = (RentalSession) rentalSessions.get(clientName);
        if (session != null) {
            session.removeQuotes();
            session.terminate();
            rentalSessions.remove(clientName);
        }
    }
}
