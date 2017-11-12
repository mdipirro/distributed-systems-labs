package session;

import java.util.List;
import java.util.Set;
import javax.ejb.Remote;
import rental.CarType;

@Remote
public interface ManagerSessionRemote {
    
    Set<CarType> getCarTypes(String company);
    Set<Integer> getCarIds(String company,String type);
    int getNumberOfReservations(String company, String type, int carId);
    int getNumberOfReservations(String company, String type);  
    void loadRental(String datafile);
    void addRentalCompany(String name, List<String> regions);
    void addCar(String companyName, CarType carType);
    void addCarType(String companyName, CarType carType);
}