package session;

import rental.*;
import java.rmi.RemoteException;
import java.util.*;
import namingservice.NamingServiceRemote;

public class ManagerSession implements ManagerSessionRemote {
    
    NamingServiceRemote namingService;

    public ManagerSession(NamingServiceRemote namingService) {
        this.namingService = namingService;
    }

    @Override
    public synchronized void register(CarRentalCompanyRemote company) throws RemoteException {
        namingService.addCompany(company);
    }

    @Override
    public synchronized void unregister(String companyName) throws RemoteException {
        namingService.removeCompany(companyName);
    }

    @Override
    public List<CarRentalCompanyRemote> getRentals() throws RemoteException {
        return new LinkedList<>(namingService.getRentals().values());
    }

    @Override
    public List<CarType> getCarTypesByCompany(String companyName) throws RemoteException {
        return new LinkedList<>(namingService.getRental(companyName).getAllCarTypes());
    }

    @Override
    public int getNumberOfReservations(String carType, String companyName) throws RemoteException {
        int num = 0;
        List<Car> cars = namingService.getRental(companyName).getCars();
        for(Car car: cars){
            if(car.getType().getName().equals(carType)){
                num += car.getReservations().size();
            }
        }
        return num;
    }

    @Override
    public Set<String> getBestCostumers() throws RemoteException {
        Set<String> bestCustomers = new HashSet<String>();
        int max = 0;
        Map<String, Integer> customersReservationsNum = new HashMap<>();
        for(CarRentalCompanyRemote company : namingService.getRentals().values()){
            for(Car car: company.getCars()){
                for(Reservation reservation : car.getReservations()){
                    if(customersReservationsNum.containsKey(reservation.getCarRenter())){
                        customersReservationsNum.put(reservation.getCarRenter(), customersReservationsNum.get(reservation.getCarRenter())+1);
                    }
                    else{
                        customersReservationsNum.put(reservation.getCarRenter(), 1);
                    }
                    if(max <= customersReservationsNum.get(reservation.getCarRenter())){
                        if(max < customersReservationsNum.get(reservation.getCarRenter())) {
                            bestCustomers.clear();
                            max = customersReservationsNum.get(reservation.getCarRenter());
                        }
                        bestCustomers.add(reservation.getCarRenter());
                    }
                }
            }
        }
        return bestCustomers;
    }

    @Override
    public CarType getMostPopularCarType(String companyName, int year) throws RemoteException {
        Map<CarType, Integer> popularity = new HashMap<>();
        int max = 0;
        CarType mostPopular = null;
        Calendar calendar = Calendar.getInstance();
        for (Car car : namingService.getRental(companyName).getCars()) {
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

    @Override
    public String getMostPopularCarRentalCompany() throws RemoteException {
        int max = 0;
        String mostPopular = null;
        for (CarRentalCompanyRemote company: namingService.getRentals().values()){
            int numberOfReservation = 0;
            for (Car car : company.getCars()) {
                numberOfReservation+=car.getReservations().size();
            }
            if(numberOfReservation > max){
                max = numberOfReservation;
                mostPopular = company.getName();
            }
        }
        return mostPopular;
    }

}
