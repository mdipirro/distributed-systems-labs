package server;

import rental.*;
import rentalstore.NamingService;
import rentalstore.NamingServiceRemote;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RentalServer {

    public static final String MANAGER_SESSION_NAME = "MANAGER_SESSION";
	public static final String RENTAL_SESSION_MANAGER_NAME = "RENTAL_SESSION_MANAGER";
    public static final String NAMING_SERVICE_NAME = "NAMING_SERVICE";

	public static void main(String[] args) throws ReservationException,
			NumberFormatException, IOException {
		ManagerSession mgr = new ManagerSession();
		RentalSessionManager rtlManager = new RentalSessionManager();
        NamingService namingService = new NamingService();

		System.setSecurityManager(null);
		try {
			ManagerSessionRemote msStub = (ManagerSessionRemote)
                    UnicastRemoteObject.exportObject(mgr, 0);
            RentalSessionManagerRemote rmgrStub = (RentalSessionManagerRemote)
                    UnicastRemoteObject.exportObject(rtlManager, 0);
            NamingServiceRemote namingStub = (NamingServiceRemote)
                    UnicastRemoteObject.exportObject(namingService, 0);
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(MANAGER_SESSION_NAME, msStub);
            registry.rebind(RENTAL_SESSION_MANAGER_NAME, rmgrStub);
            registry.rebind(NAMING_SERVICE_NAME, namingService);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
