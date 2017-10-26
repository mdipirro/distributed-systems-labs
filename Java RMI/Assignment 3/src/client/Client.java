package client;

import rental.CarRentalCompany;
import rental.CarType;
import server.ManagerSessionRemote;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    private ManagerSessionRemote managerSession;

    public static void main(String[] args) {
        Client main = new Client();
        main.run();
    }

    public Client() {
        Registry registry;
        try {
            System.setSecurityManager(null);
            registry = LocateRegistry.getRegistry();
            managerSession = (ManagerSessionRemote) registry.lookup("MANAGER_SESSION");
        } catch (Exception e) {
            System.err.println("EXCEPTION during client creation:");
            e.printStackTrace();
        }
    }

    private void run() {
        try {
            for (CarRentalCompany company : managerSession.getRentals()) {
                System.out.println("COMPANY: " + company.getName());
                for (CarType carType : managerSession.getCarTypesByCompany(company.getName())) {
                    System.out.println("   -) " + carType);
                }
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
