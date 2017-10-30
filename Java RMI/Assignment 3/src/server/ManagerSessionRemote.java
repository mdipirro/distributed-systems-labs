package server;

import rental.CarRentalCompany;
import rental.CarRentalCompanyRemote;
import rental.CarType;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

public interface ManagerSessionRemote extends Remote {
    void register (CarRentalCompanyRemote company) throws RemoteException;
    void unregister (String companyName) throws RemoteException;
    List<CarRentalCompanyRemote> getRentals() throws RemoteException;
    List<CarType> getCarTypesByCompany(String companyName) throws RemoteException;
    int getNumberOfReservations(String carType, String companyName) throws RemoteException;
    Set<String> getBestCostumer() throws RemoteException;
    CarType getMostPopularCarType(String companyName, int year) throws RemoteException;
}
