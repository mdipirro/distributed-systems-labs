package server;

import rental.*;
import rentalstore.NamingService;

import java.rmi.RemoteException;
import java.util.*;

public class RentalSession implements RentalSessionRemote {

    private String clientName;
    private List<Quote> quotes;

    public RentalSession(String clientName) {
        this.clientName = clientName;
        quotes = new LinkedList<>();
    }

    @Override
    public Quote createQuote(ReservationConstraints constraints) throws ReservationException, RemoteException {
        boolean companyFound = false;
        Iterator<CarRentalCompany> iterator = NamingService.getRentals().values().iterator();
        CarRentalCompany company = null;
        // Look for a company having the requested region
        while (iterator.hasNext() && !companyFound) {
            company = iterator.next();
            companyFound = (company.hasRegion(constraints.getRegion())) &&
                    company.isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate());
        }
        if (company != null) { // A company has been found
            Quote quote = company.createQuote(constraints, clientName);
            quotes.add(quote);
            return quote;
        }
        // No companies with the requested region, throw an exception
        throw new ReservationException("No available company with the "
                + "requested region <" + constraints.getRegion() + ">");
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
                        NamingService.getRental(quote.getRentalCompany()).confirmQuote(quote)
                );
            }
            return reservations;
        } catch (ReservationException exc) {
            for (Reservation reservation : reservations) {
                NamingService.getRental(reservation.getRentalCompany()).cancelReservation(reservation);
            }
            quotes.remove(failed);
            throw exc;
        }
    }

    @Override
    public List<CarType> getAvailableCarTypes(Date start, Date end) throws RemoteException {
        List<CarType> availableCarTypes = new ArrayList<CarType>();
        for (CarRentalCompany company : NamingService.getRentals().values()) {
            availableCarTypes.addAll(company.getAvailableCarTypes(start, end));
        }
        return availableCarTypes;
    }

    @Override
    public CarType getCheapestCarType(Date start, Date end) throws RemoteException {
        CarType cheapest = null;
        double minPrice = 0;
        for (CarRentalCompany company : NamingService.getRentals().values()) {
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
