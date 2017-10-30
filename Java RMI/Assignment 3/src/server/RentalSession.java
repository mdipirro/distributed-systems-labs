package server;

import rental.*;
import rentalstore.NamingService;
import rentalstore.NamingServiceRemote;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class RentalSession implements RentalSessionRemote {

    private String clientName;
    private List<Quote> quotes;
    private NamingServiceRemote namingService;

    public RentalSession(String clientName) {
        this.clientName = clientName;
        quotes = new LinkedList<>();
        System.setSecurityManager(null);
        try {
            Registry registry = LocateRegistry.getRegistry();
            namingService = (NamingServiceRemote)
                    registry.lookup(RentalServer.RENTAL_SESSION_MANAGER_NAME);
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Quote createQuote(ReservationConstraints constraints) throws ReservationException, RemoteException {
        CarRentalCompanyRemote company = findSuitableCompany(constraints);
        if (company != null) { // A company has been found
            Quote quote = company.createQuote(constraints, clientName);
            quotes.add(quote);
            return quote;
        }
        throw new ReservationException("No available company with the "
                + "requested constraints <" + constraints.getRegion() + ">");
    }

    private CarRentalCompanyRemote findSuitableCompany(ReservationConstraints constraints) throws RemoteException {
        boolean companyFound = false;
        Iterator<CarRentalCompanyRemote> iterator = namingService.getRentals().values().iterator();
        CarRentalCompanyRemote company = null;
        while (iterator.hasNext() && !companyFound) {
            company = iterator.next();
            companyFound = (company.hasRegion(constraints.getRegion())) &&
                    company.isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate());
        }
        return company;
    }

    @Override
    public List<Quote> getCurrentQuotes() throws RemoteException {
        return quotes;
    }

    @Override
    public List<Reservation> confirmQuotes() throws ReservationException, RemoteException {
        // Allocate a list of reservation with the same siza as quotes
        List<Reservation> reservations = new ArrayList<Reservation>(quotes.size());
        Quote failed = null;
        try {
            // For each quote, try to confirm it
            for (Quote quote : quotes) {
                failed = quote;
                reservations.add(
                        namingService.getRental(quote.getRentalCompany()).confirmQuote(quote)
                );
            }
            return reservations;
        } catch (ReservationException exc) {
            for (Reservation reservation : reservations) {
                namingService.getRental(reservation.getRentalCompany()).cancelReservation(reservation);
            }
            quotes.remove(failed);
            throw exc;
        }
    }

    @Override
    public List<CarType> getAvailableCarTypes(Date start, Date end) throws RemoteException {
        List<CarType> availableCarTypes = new ArrayList<CarType>();
        for (CarRentalCompanyRemote company : namingService.getRentals().values()) {
            availableCarTypes.addAll(company.getAvailableCarTypes(start, end));
        }
        return availableCarTypes;
    }

    @Override
    public CarType getCheapestCarType(Date start, Date end) throws RemoteException {
        CarType cheapest = null;
        double minPrice = Double.MAX_VALUE;
        for (CarRentalCompanyRemote company : namingService.getRentals().values()) {
            for (CarType carType : company.getAllCarTypes()) {
                double actualPrice = company.calculateRentalPrice(carType.getRentalPricePerDay(), start, end);
                if (actualPrice < minPrice) {
                    minPrice = actualPrice;
                    cheapest = carType;
                }
            }
        }
        return cheapest;
    }
}
