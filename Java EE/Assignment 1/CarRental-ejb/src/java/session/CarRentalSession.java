package session;

import java.util.HashSet;
import java.util.Set;
import javax.ejb.Stateful;
import rental.RentalStore;

@Stateful
public class CarRentalSession implements CarRentalSessionRemote {

    @Override
    public Set<String> getAllRentalCompanies() {
        return new HashSet<String>(RentalStore.getRentals().keySet());
    }

    
    
}
