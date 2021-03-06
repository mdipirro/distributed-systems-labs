package session; 
 
import java.util.List; 
import javax.ejb.Remote; 
import rental.CarType;
 
/** 
 * 
 * @author jakub 
 */ 
 
@Remote 
public interface ManagerSessionRemote { 
     
    List<CarType> getCarTypes(String companyName); 
    int getNumberOfReservationsForCarType(String carType, String companyName); 
    String getBestCustomer(String companyName);
    int getNumberOfReservationsBy(String name);
     
} 