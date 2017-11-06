package namingservice;

import rental.CarRentalCompanyRemote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface NamingServiceRemote extends Remote {
    CarRentalCompanyRemote getRental(String company) throws RemoteException;
    Map<String, CarRentalCompanyRemote> getRentals() throws RemoteException;
    void addCompany(CarRentalCompanyRemote company) throws RemoteException;
    void removeCompany(String companyName) throws RemoteException;
}
