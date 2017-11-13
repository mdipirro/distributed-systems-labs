package session;

import java.util.List;
import java.util.Set;
import javax.ejb.Remote;
import rental.Car;
import rental.CarType;

@Remote
public interface ManagerSessionRemote {
    
    Set<CarType> getCarTypes(String company);
    Set<Integer> getCarIds(String company,String type);
    int getNumberOfReservations(String company, String type, int carId);
    int getNumberOfReservations(String company, String type);  
    void loadRental(String datafile);
    void addRentalCompany(String name, List<String> regions);
    void addRentalCompany(String name, List<String> regions, List<Car> types);
    void addCar(String companyName, Car carType);
    void addCarType(String companyName, CarType carType);
    String test();
    public CarType getMostPopularCarType(String carRentalCompanyName, int year);
    public Set<String> getBestClients();
}