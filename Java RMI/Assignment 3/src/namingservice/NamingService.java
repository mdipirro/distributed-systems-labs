package namingservice;

import rental.Car;
import rental.CarRentalCompany;
import rental.CarRentalCompanyRemote;
import rental.CarType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NamingService implements NamingServiceRemote {

    private Map<String, CarRentalCompanyRemote> rentals;
    
    public NamingService(){
        rentals = new ConcurrentHashMap<String, CarRentalCompanyRemote>();
        loadRental("hertz.csv");
        loadRental("dockx.csv");
    }

    @Override
    public CarRentalCompanyRemote getRental(String company) {
        CarRentalCompanyRemote out = getRentals().get(company);
        if (out == null) {
            throw new IllegalArgumentException("Company doesn't exist!: " + company);
        }
        return out;
    }

    @Override
    public Map<String, CarRentalCompanyRemote> getRentals(){
        return rentals;
    }

    @Override
    public void addCompany(CarRentalCompanyRemote company) throws RemoteException {
        rentals.put(company.getName(), company);
    }

    @Override
    public void removeCompany(String companyName) {
        rentals.remove(companyName);
    }

    private void loadRental(String datafile) {
        try {
            CrcData data = loadData(datafile);
            CarRentalCompany company = new CarRentalCompany(data.name, data.regions, data.cars);
            CarRentalCompanyRemote stub = (CarRentalCompanyRemote)
                UnicastRemoteObject.exportObject(company, 0);
            rentals.put(data.name, stub);
            Logger.getLogger(NamingService.class.getName()).log(Level.INFO, "Loaded {0} from file {1}", new Object[]{data.name, datafile});
        } catch (NumberFormatException ex) {
            Logger.getLogger(NamingService.class.getName()).log(Level.SEVERE, "bad file", ex);
        } catch (IOException ex) {
            Logger.getLogger(NamingService.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private CrcData loadData(String datafile) throws IOException {

        CrcData out = new CrcData();
        int nextuid = 0;

        // open file
        BufferedReader in = new BufferedReader(new FileReader(datafile));
        StringTokenizer csvReader;

        try {
            // while next line exists
            while (in.ready()) {
                String line = in.readLine();

                if (line.startsWith("#")) {
                    // comment -> skip
                } else if (line.startsWith("-")) {
                    csvReader = new StringTokenizer(line.substring(1), ",");
                    out.name = csvReader.nextToken();
                    out.regions = Arrays.asList(csvReader.nextToken().split(":"));
                } else {
                    // tokenize on ,
                    csvReader = new StringTokenizer(line, ",");
                    // create new car type from first 5 fields
                    CarType type = new CarType(csvReader.nextToken(),
                            Integer.parseInt(csvReader.nextToken()),
                            Float.parseFloat(csvReader.nextToken()),
                            Double.parseDouble(csvReader.nextToken()),
                            Boolean.parseBoolean(csvReader.nextToken()));
                    System.out.println(type);
                    // create N new cars with given type, where N is the 5th field
                    for (int i = Integer.parseInt(csvReader.nextToken()); i > 0; i--) {
                        out.cars.add(new Car(nextuid++, type));
                    }
                }
            }
        } finally {
            in.close();
        }

        return out;
    }

    static class CrcData {
            public List<Car> cars = new LinkedList<Car>();
            public String name;
            public List<String> regions =  new LinkedList<String>();
    }
}