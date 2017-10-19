package client;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import rental.CarType;
import rental.Reservation;
import rental.ReservationConstraints;
import session.CarRentalSessionRemote;
import session.ManagerSessionRemote;

public class Main extends AbstractTestAgency {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new Main("simpleTrips").run();
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Main(String scriptFile) {
        super(scriptFile);
    }

    @Override
    protected CarRentalSessionRemote getNewReservationSession(String name) throws Exception {
        InitialContext context = new InitialContext();
        return (CarRentalSessionRemote) context.lookup(CarRentalSessionRemote.class.getName());
    }

    @Override
    protected Object getNewManagerSession(String name, String carRentalName) throws Exception {
        InitialContext context = new InitialContext();
        return (ManagerSessionRemote) context.lookup(ManagerSessionRemote.class.getName());
    }

    @Override
    protected void checkForAvailableCarTypes(Object session, Date start, Date end) throws Exception {
        CarRentalSessionRemote bean = (CarRentalSessionRemote)session;
        for(CarType carType : bean.getAvailableCarTypes(start, end)) {
            System.out.println(carType);
        }
    }

    @Override
    protected void addQuoteToSession(Object session, String name, Date start, Date end, String carType, String region) throws Exception {
        CarRentalSessionRemote bean = (CarRentalSessionRemote)session;
        bean.createQuote(
                new ReservationConstraints(start, end, carType, region),
                name
        );
    }

    @Override
    protected List<Reservation> confirmQuotes(Object session, String name) throws Exception {
        CarRentalSessionRemote bean = (CarRentalSessionRemote)session;
        return bean.confirmQuotes();
    }

    @Override
    protected int getNumberOfReservationsForCarType(Object ms, String carRentalName, String carType) throws Exception {
        ManagerSessionRemote bean = (ManagerSessionRemote)ms;
        return bean.getNumberOfReservationsForCarType(carType, carRentalName);
    }

    @Override
    protected int getNumberOfReservationsBy(Object ms, String clientName) throws Exception {
        ManagerSessionRemote bean = (ManagerSessionRemote)ms;
        return bean.getNumberOfReservationsBy(clientName);
    }

    @Override
    protected String getBestCustomer(Object ms) {
        ManagerSessionRemote bean = (ManagerSessionRemote)ms;
        return bean.getBestCustomer();
    }    

    @Override
    protected List getCarTypes(Object ms, String carRentalName){
        ManagerSessionRemote bean = (ManagerSessionRemote)ms;
        return bean.getCarTypes(carRentalName);
    }
    
}
