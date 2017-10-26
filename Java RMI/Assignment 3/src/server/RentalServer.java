package server;

import rental.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class RentalServer {

    private static final String MGR_NAME = "MANAGER_SESSION";

	public static void main(String[] args) throws ReservationException,
			NumberFormatException, IOException {
		ManagerSession mgr = new ManagerSession();

		System.setSecurityManager(null);
		try {
			ManagerSessionI mgrStub = (ManagerSessionI) UnicastRemoteObject.exportObject(mgr, 0);
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(MGR_NAME, mgrStub);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
