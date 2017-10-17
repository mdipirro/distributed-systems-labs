package client;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import rental.Quote;
import rental.ReservationConstraints;
import rental.ReservationException;
import session.CarRentalSessionRemote;

public class Main {
    
    @EJB
    static CarRentalSessionRemote session;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("found rental companies: "+session.getAllRentalCompanies());
        
    }
}
