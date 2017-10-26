package server;

import rental.*;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RentalServer {

    private static final String MGR_NAME = "MANAGER_SESSION";

	public static void main(String[] args) throws ReservationException,
			NumberFormatException, IOException {
		ManagerSession mgr = new ManagerSession();

		System.setSecurityManager(null);
		try {
			ManagerSessionRemote mgrStub = (ManagerSessionRemote) UnicastRemoteObject.exportObject(mgr, 0);
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(MGR_NAME, mgrStub);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
