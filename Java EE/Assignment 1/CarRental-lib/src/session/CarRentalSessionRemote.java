package session;

import java.util.Set;
import javax.ejb.Remote;

@Remote
public interface CarRentalSessionRemote {

    Set<String> getAllRentalCompanies();
    
}
