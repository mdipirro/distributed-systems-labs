package session;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AgencySessionManagerRemote extends Remote {
    RentalSessionRemote getRentalSession(String clientName) throws RemoteException;
    void terminateRentalSession(String clientName) throws RemoteException;
}
