package client;

import javax.ejb.EJB;
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
