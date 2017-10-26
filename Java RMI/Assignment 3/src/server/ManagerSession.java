package server;

import rental.*;
import rentalstore.NamingService;

import java.rmi.RemoteException;
import java.util.*;

public class ManagerSession implements ManagerSessionI {
    @Override
    public void register(CarRentalCompany company) throws RemoteException {
        // TODO
    }

    @Override
    public void unregister(String companyName) throws RemoteException {
        // TODO
    }

    @Override
    public List<CarRentalCompany> getRentals() throws RemoteException {
        return new LinkedList<>(NamingService.getRentals().values());
    }

    @Override
    public List<CarType> getCarTypesByCompany(String companyName) throws RemoteException {
        return new LinkedList<>(NamingService.getRental(companyName).getAllCarTypes());
    }

    @Override
    public int getNumberOfReservations(CarType carType, String companyName) throws RemoteException {
        int num = 0;
        List<Car> cars = NamingService.getRental(companyName).getCars();
        for(Car car: cars){
            if(car.getType().getName().equals(carType)){
                num += car.getReservations().size();
            }
        }
        return num;
    }

    @Override
    public String getBestCostumer(String companyName) throws RemoteException {
        String customerName = null;
        int max = 0;
        Map<String, Integer> customersReservationsNum = new HashMap<>();
        for(Car car: NamingService.getRental(companyName).getCars()){
            for(Reservation reservation : car.getReservations()){
                if(customersReservationsNum.containsKey(reservation.getCarRenter())){
                    customersReservationsNum.put(reservation.getCarRenter(), customersReservationsNum.get(reservation.getCarRenter())+1);
                }
                else{
                    customersReservationsNum.put(reservation.getCarRenter(), 1);
                }
                if(max<=customersReservationsNum.get(reservation.getCarRenter())){
                    max = customersReservationsNum.get(reservation.getCarRenter());
                    customerName = reservation.getCarRenter();
                }
            }
        }
        return customerName;
    }

    @Override
    public CarType getMostPopularCarType(String companyName, int year) {
        Map<CarType, Integer> popularity = new HashMap<>();
        int max = 0;
        CarType mostPopular = null;
        Calendar calendar = Calendar.getInstance();
        for (Car car : NamingService.getRental(companyName).getCars()) {
            CarType carType = car.getType();
            for (Reservation reservation : car.getReservations()) {
                calendar.setTime(reservation.getStartDate());
                if (calendar.get(Calendar.YEAR) == year) {
                    if (popularity.containsKey(carType)) {
                        popularity.put(carType, popularity.get(carType) + 1);
                    } else {
                        popularity.put(carType, 1);
                    }
                    int reservations = popularity.get(carType);
                    if (reservations > max) {
                        max = reservations;
                        mostPopular = carType;
                    }
                }
            }
        }
        return mostPopular;
    }
}
