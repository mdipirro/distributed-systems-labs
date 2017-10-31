package server;

import configuration.AgencyConfigRMI;
import rental.*;
import namingservice.NamingService;
import namingservice.NamingServiceRemote;
import session.AgencySessionManager;
import session.AgencySessionManagerRemote;
import session.ManagerSession;
import session.ManagerSessionRemote;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RentalServer {

    Registry registry;

    public static void main(String[] args) throws ReservationException,
            NumberFormatException, IOException {
        System.setSecurityManager(null);
        RentalServer rentalServer = new RentalServer();
        rentalServer.setupDependencies();

        System.out.println("Rental agency started SUCCESSFULLY");
    }

    private void setupDependencies(){
        try {
            registry = LocateRegistry.getRegistry();

            NamingService ns = new NamingService();
            NamingServiceRemote namingServiceStub = (NamingServiceRemote)
                    UnicastRemoteObject.exportObject(ns, 0);

            AgencySessionManager asm = new AgencySessionManager(namingServiceStub);
            AgencySessionManagerRemote asmStub = (AgencySessionManagerRemote)
                    UnicastRemoteObject.exportObject(asm, 0);

            ManagerSession ms = new ManagerSession(ns);
            ManagerSessionRemote msStub = (ManagerSessionRemote)
                    UnicastRemoteObject.exportObject(ms, 0);

            registry.rebind(AgencyConfigRMI.NAMING_SERVICE_NAME, namingServiceStub);
            registry.rebind(AgencyConfigRMI.AGENCY_SESSION_MANAGER_NAME, asmStub);
            registry.rebind(AgencyConfigRMI.MANAGER_SESSION, msStub);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
