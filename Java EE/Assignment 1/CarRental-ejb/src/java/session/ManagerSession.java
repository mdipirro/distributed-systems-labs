package session; 
 
import java.util.ArrayList; 
import java.util.HashMap; 
import java.util.List; 
import java.util.Map; 
import javax.ejb.Stateless; 
import rental.Car; 
import rental.CarRentalCompany; 
import rental.CarType; 
import rental.RentalStore; 
import rental.Reservation; 
 
/** 
 * 
 * @author jakub 
 */ 
 
@Stateless 
public class ManagerSession implements ManagerSessionRemote{ 
 
    @Override 
    public List<CarType> getCarTypes(String companyName) { 
        CarRentalCompany company = RentalStore.getRental(companyName); 
        return new ArrayList(company.getAllCarTypes()); 
    } 
 
    @Override 
    public int getNumberOfReservationsForCarType(String carType,String companyName) { 
        int num = 0; 
        List<Car> cars = RentalStore.getRental(companyName).getCars(); 
        for(Car car: cars){ 
            if(car.getType().getName().equals(carType)){ 
                num += car.getAllReservations().size(); 
            }             
        }             
        return num; 
    } 
 
    @Override 
    public String getBestCustomer() { 
        String customerName = null; 
        int max = 0; 
        Map<String, Integer> customersReservationsNum = new HashMap<String, Integer>(); 
        for(Map.Entry<String, CarRentalCompany> entry : RentalStore.getRentals().entrySet()){ 
            for(Car car: entry.getValue().getCars()){ 
                for(Reservation reservation : car.getAllReservations()){ 
                    if(customersReservationsNum.containsKey(reservation.getCarRenter())){ 
                        customersReservationsNum.put(reservation.getCarRenter(), customersReservationsNum.get(reservation.getCarRenter())+1); 
                    } 
                    else{ 
                        customersReservationsNum.put(reservation.getCarRenter(), 1); 
                    } 
                    if(max<=customersReservationsNum.get(reservation.getCarRenter())){     //TODO change -> only equals 
                        max = customersReservationsNum.get(reservation.getCarRenter()); 
                        customerName = reservation.getCarRenter(); 
                    } 
                } 
            } 
        } 
        return customerName; 
    } 
     
} 