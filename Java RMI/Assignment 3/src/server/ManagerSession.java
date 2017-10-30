package server;

import rental.*;
import rentalstore.NamingService;
import rentalstore.NamingServiceRemote;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class ManagerSession implements ManagerSessionRemote {

    private NamingServiceRemote getNamingService() throws RemoteException {
        System.setSecurityManager(null);
        Registry registry = LocateRegistry.getRegistry();
        try {
            return (NamingServiceRemote) registry.lookup(RentalServer.NAMING_SERVICE_NAME);
        } catch (NotBoundException e) {
            throw new RemoteException("Exception during NamingService lookup");
        }
    }

    @Override
    public synchronized void register(CarRentalCompanyRemote company) throws RemoteException {
        // TODO
        getNamingService().addCompany(company);
    }

    @Override
    public synchronized void unregister(String companyName) throws RemoteException {
        // TODO
        getNamingService().removeCompany(companyName);
    }

    @Override
    public List<CarRentalCompanyRemote> getRentals() throws RemoteException {
        return new LinkedList<>(getNamingService().getRentals().values());
    }

    @Override
    public List<CarType> getCarTypesByCompany(String companyName) throws RemoteException {
        return new LinkedList<>(getNamingService().getRental(companyName).getAllCarTypes());
    }

    @Override
    public int getNumberOfReservations(String carType, String companyName) throws RemoteException {
        int num = 0;
        List<Car> cars = getNamingService().getRental(companyName).getCars();
        for(Car car: cars){
            if(car.getType().getName().equals(carType)){
                num += car.getReservations().size();
            }
        }
        return num;
    }

    @Override
    public Set<String> getBestCostumer() throws RemoteException {
        Set<String> bestCustomers = new HashSet<String>();
        int max = 0;
        Map<String, Integer> customersReservationsNum = new HashMap<>();
        for(CarRentalCompanyRemote company : getNamingService().getRentals().values()){
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
        for (Car car : getNamingService().getRental(companyName).getCars()) {
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
