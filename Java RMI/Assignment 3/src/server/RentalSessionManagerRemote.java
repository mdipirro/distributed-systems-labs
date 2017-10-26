package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RentalSessionManagerRemote extends Remote {
    RentalSessionRemote add(String clientName) throws RemoteException;
    void remove(String clientName) throws RemoteException;
}
