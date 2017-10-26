package server;

import rental.CarRentalCompany;
import rental.CarType;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ManagerSessionI extends Remote {
    void register (CarRentalCompany company) throws RemoteException;
    void unregister (String companyName) throws RemoteException;
    List<CarRentalCompany> getRentals() throws RemoteException;
    List<CarType> getCarTypesByCompany(String companyName) throws RemoteException;
    int getNumberOfReservations(CarType carType, String companyName) throws RemoteException;
    String getBestCostumer(String companyName) throws RemoteException;
    CarType getMostPopularCarType(String companyName, int year) throws RemoteException;
}
