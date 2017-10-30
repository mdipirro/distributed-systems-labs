package server;

import rental.CarRentalCompanyRemote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class RentalSessionManager implements RentalSessionManagerRemote {

    private Map<String, RentalSessionRemote> sessions;

    public RentalSessionManager() {
        sessions = new HashMap<>();
    }

    @Override
    public RentalSessionRemote add(String clientName) throws RemoteException {
        // TODO What to do if the client asks for a new session with an existing one?
        RentalSession session = new RentalSession(clientName);
        RentalSessionRemote stub = (RentalSessionRemote)
                UnicastRemoteObject.exportObject(session, 0);
        sessions.put(clientName, stub);
        return session;
    }

    @Override
    public void remove(String clientName) throws RemoteException {
        sessions.remove(clientName);
    }
}
