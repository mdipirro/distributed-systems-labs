package client;

import java.util.Date;
import java.util.List;
import javax.naming.InitialContext;
import rental.Reservation;
import rental.ReservationConstraints;
import session.CarRentalSessionRemote;

public class Main extends AbstractTestAgency {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {}

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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void checkForAvailableCarTypes(Object session, Date start, Date end) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void addQuoteToSession(Object session, String name, Date start, Date end, String carType, String region) throws Exception {
        if (session instanceof CarRentalSessionRemote) {
            CarRentalSessionRemote bean = (CarRentalSessionRemote)session;
            bean.createQuote(
                    new ReservationConstraints(start, end, carType, region),
                    name
            );
        }
        throw new ClassCastException("Wrong session object provided");
    }

    @Override
    protected List<Reservation> confirmQuotes(Object session, String name) throws Exception {
        if (session instanceof CarRentalSessionRemote) {
            CarRentalSessionRemote bean = (CarRentalSessionRemote)session;
            return bean.confirmQuotes();
        }
        throw new ClassCastException("Wrong session object provided");
    }

    @Override
    protected int getNumberOfReservationsForCarType(Object ms, String carRentalName, String carType) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
