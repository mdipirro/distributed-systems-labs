package rental;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Car {

    private int id;
    private CarType type;
    private List<Reservation> reservations;

    /***************
     * CONSTRUCTOR *
     ***************/
    
    public Car(int uid, CarType type) {
    	this.id = uid;
        this.type = type;
        this.reservations = new ArrayList<Reservation>();
    }

    /******
     * ID *
     ******/
    
    public int getId() {
    	return id;
    }
    
    /************
     * CAR TYPE *
     ************/
    
    public CarType getType() {
        return type;
    }

    /****************
     * RESERVATIONS *
     ****************/

    public boolean isAvailable(Date start, Date end) {
        if(!start.before(end))
            throw new IllegalArgumentException("Illegal given period");

        for(Reservation reservation : reservations) {
            if(reservation.getEndDate().before(start) || reservation.getStartDate().after(end))
                continue;
            return false;
        }
        return true;
    }
    
    public void addReservation(Reservation res) {
        reservations.add(res);
    }
    
    public void removeReservation(Reservation reservation) {
        // equals-method for Reservation is required!
        reservations.remove(reservation);
    }

    /**
     * Get all the reservations corresponding to the car.
     * @return A list containing the reservations
     */
    public List<Reservation> getReservations() {
        /*
        This method performs a deep copy instead of returning a reference
        to the reservations list. In this way it is not possible, for a
        whichever user, to modify the reservations in the list.
         */
    	List<Reservation> copy = new ArrayList<>(reservations.size());
    	for (Reservation r : reservations) {
    		copy.add(new Reservation(
    				new Quote(
    						r.getCarRenter(),
    						r.getStartDate(),
    						r.getEndDate(),
    						r.getRentalCompany(),
    						r.getCarType(),
    						r.getRentalPrice()
    				), r.getCarId()));
    	}
    	return copy;
    }
}