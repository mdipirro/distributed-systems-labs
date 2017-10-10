package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.List;
import java.util.Set;

import rental.*;

public class Client extends AbstractTestBooking {

	private CarRentalCompanyI comp;

	/********
	 * MAIN *
	 ********/

	public static void main(String[] args) throws Exception {

		String carRentalCompanyName = "CarRentalCompany";

		//System.setSecurityManager(new SecurityManager());
		if (System.getSecurityManager() == null) {
			//System.setSecurityManager(new SecurityManager());
			System.out.println("Security manager je vypnuty");
		}

		// An example reservation scenario on car rental company 'Hertz' would be...
		Client client = new Client("simpleTrips", carRentalCompanyName);
		client.run();
	}

	/***************
	 * CONSTRUCTOR *
	 ***************/

	public Client(String scriptFile, String carRentalCompanyName) {
		super(scriptFile);
		Registry registry;
		try {
			registry = LocateRegistry.getRegistry();
			comp = (CarRentalCompanyI) registry.lookup(carRentalCompanyName);        //TODO pouzit mattheo's interface, jaky nazov tam pouzil?
		} catch (Exception e) {
			System.err.println("EXCEPTION during client creation:");
			e.printStackTrace();
		}
	}

	/**
	 * Check which car types are available in the given period
	 * and print this list of car types.
	 *
	 * @param     start
	 *             start time of the period
	 * @param     end
	 *             end time of the period
	 * @throws     Exception
	 *             if things go wrong, throw exception
	 */
	@Override
	protected void checkForAvailableCarTypes(Date start, Date end) throws Exception {
		Set<CarType> availableTypes = comp.getAvailableCarTypes(start, end);
		for (CarType carType : availableTypes) {
		    System.out.println(carType);
        }
	}

	/**
	 * Retrieve a quote for a given car type (tentative reservation).
	 *
	 * @param    clientName
	 *             name of the client
	 * @param     start
	 *             start time for the quote
	 * @param     end
	 *             end time for the quote
	 * @param     carType
	 *             type of car to be reserved
	 * @param     region
	 *             region in which car must be available
	 * @return    the newly created quote
	 *
	 * @throws     Exception
	 *             if things go wrong, throw exception
	 */
	@Override
	protected Quote createQuote(String clientName, Date start, Date end,
								String carType, String region) throws Exception {
	    Quote quote = comp.createQuote(new ReservationConstraints(start, end, carType, region), clientName);
	    System.out.println(quote);
		return quote;
	}

	/**
	 * Confirm the given quote to receive a final reservation of a car.
	 *
	 * @param     quote
	 *             the quote to be confirmed
	 * @return    the final reservation of a car
	 *
	 * @throws     Exception
	 *             if things go wrong, throw exception
	 */
	@Override
	protected Reservation confirmQuote(Quote quote) throws Exception {
		return comp.confirmQuote(quote);
	}

	/**
	 * Get all reservations made by the given client.
	 *
	 * @param     clientName
	 *             name of the client
	 * @return    the list of reservations of the given client
	 *
	 * @throws     Exception
	 *             if things go wrong, throw exception
	 */
	@Override
	protected List<Reservation> getReservationsByRenter(String clientName) throws Exception {
	    List<Reservation> reservations = comp.getReservationsByRenter(clientName);
		for (Reservation r : reservations) {
			System.out.println(r.getCarType() + " " + r.getCarId() + " " + r.getStartDate() + " " +
			r.getEndDate() + " " + r.getRentalPrice());
		}
		return reservations;
	}

	/**
	 * Get the number of reservations for a particular car type.
	 *
	 * @param     carType
	 *             name of the car type
	 * @return     number of reservations for the given car type
	 *
	 * @throws     Exception
	 *             if things go wrong, throw exception
	 */
	@Override
	protected int getNumberOfReservationsForCarType(String carType) throws Exception {
        return comp.getReservationsByCarType(carType).size();
	}
}