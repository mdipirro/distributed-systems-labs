package session;

import rental.*;
import namingservice.NamingServiceRemote;

import java.rmi.RemoteException;
import java.util.*;

public class RentalSession implements RentalSessionRemote {

    private String clientName;
    private NamingServiceRemote namingService;
    private List<Quote> quotes;
    private Date creationDate;

    public RentalSession(String clientName, NamingServiceRemote namingService) {
        this.clientName = clientName;
        this.namingService = namingService;
        this.quotes = new LinkedList<>();
        creationDate = new Date();
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
                    company.hasCarType(constraints.getCarType()) &&
                    company.isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate());
        }
        return company;
    }

    @Override
    public List<Quote> getCurrentQuotes() throws RemoteException {
        return quotes;
    }

    @Override
    public synchronized List<Reservation> confirmQuotes() throws ReservationException, RemoteException {
        // Allocate a list of reservation with the same size as quotes
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
            quotes.clear();
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
    public CarType getCheapestCarType(Date start, Date end, String region) throws RemoteException {
        CarType cheapest = null;
        double minPrice = Double.MAX_VALUE;
        for (CarRentalCompanyRemote company : namingService.getRentals().values()) {
            if (company.hasRegion(region)) {
                for (CarType carType : company.getAllCarTypes()) {
                    if (company.isAvailable(carType.getName(), start, end)) {
                        double actualPrice = carType.getRentalPricePerDay();//company.calculateRentalPrice(carType.getRentalPricePerDay(), start, end);
                        if (actualPrice < minPrice) {
                            minPrice = actualPrice;
                            cheapest = carType;
                        }
                    }
                }
            }
        }
        return cheapest;
    }
}
