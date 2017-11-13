package session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;

@Stateless
public class ManagerSession implements ManagerSessionRemote {
    
    @PersistenceContext
    private EntityManager em;
    
    @Override
    public Set<CarType> getCarTypes(String company) {
        return new HashSet<CarType>(
                em.createNamedQuery("getCarTypes")
                .setParameter("companyName", company)
                .getResultList()
        );
    }

    @Override
    public Set<Integer> getCarIds(String company, String type) {
        return new HashSet<Integer>(
                em.createNamedQuery("getCarIds")
                .setParameter("companyName", company)
                .setParameter("CarType", type)
                .getResultList()
        );
    }

    @Override
    public int getNumberOfReservations(String company, String type, int id) {
        return em.createNamedQuery("getNumberOfReservationsByCar")
                .setParameter("companyName", company)
                .setParameter("carId", id)
                .getFirstResult();
    }

    @Override
    public int getNumberOfReservations(String company, String type) {
        //List skuska;
        return ((Long) em.createNamedQuery("getNumberOfReservationByCarType")
//                .setParameter("companyName", company)
                .setParameter("carType", type)
                .getResultList().get(0)).intValue();
        //return 5;
    }
    
    @Override
    public void addRentalCompany(String name, List<String> regions) {
        CarRentalCompany company = new CarRentalCompany(name, regions, new LinkedList<Car>());
        em.persist(company);
    }
    
    @Override
    public void addRentalCompany(String name, List<String> regions, List<Car> cars) {
        CarRentalCompany company = new CarRentalCompany(name, regions, cars);
        em.persist(company);
    }
    
    @Override
    public void addCar(String companyName, Car car) {
        CarRentalCompany company = em.find(CarRentalCompany.class, companyName);
        //Car car = new Car();
        //car.setType(carType);
        company.addCar(car);
    }
    
    @Override
    public void addCarType(String companyName, CarType carType) {
        CarRentalCompany company = em.find(CarRentalCompany.class, companyName);
        company.addCarType(carType);
        em.persist(company);
    }

    @Override
    public String test() {
        return "Communication works!!!";
    }

    @Override
    public CarType getMostPopularCarType(String carRentalCompanyName, int year) {
        return (CarType)em.createNamedQuery("findMostPopularCarType1")
                .setParameter("companyName", carRentalCompanyName)
                .setMaxResults(1)
                //.setParameter("year", year)
                .getSingleResult();
    }

    @Override
    public Set<String> getBestClients() {           // TODO DO it better!
        int maxCount = ((Long)em.createNamedQuery("findMaxReservationCount")
                .setMaxResults(1)
                .getSingleResult()).intValue();
        return new HashSet(em.createNamedQuery("findBestCostumers")
                .setParameter("maxCount", maxCount)
                .getResultList());    
    }
    
    @Override
    public void loadRental(String datafile) {
        try {
            CrcData data = loadData(datafile);
            CarRentalCompany company = new CarRentalCompany(data.name, data.regions, data.cars);
            em.persist(company);
            Logger.getLogger(ManagerSession.class.getName()).log(Level.INFO, "Loaded {0} from file {1}", new Object[]{data.name, datafile});
        } catch (NumberFormatException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, "bad file", ex);
        } catch (IOException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static CrcData loadData(String datafile)
            throws NumberFormatException, IOException {

        CrcData out = new CrcData();
        StringTokenizer csvReader;
        int nextuid = 0;
       
        //open file from jar
        BufferedReader in = new BufferedReader(new InputStreamReader(ManagerSession.class.getClassLoader().getResourceAsStream(datafile)));
        
        try {
            while (in.ready()) {
                String line = in.readLine();
                
                if (line.startsWith("#")) {
                    // comment -> skip					
                } else if (line.startsWith("-")) {
                    csvReader = new StringTokenizer(line.substring(1), ",");
                    out.name = csvReader.nextToken();
                    out.regions = Arrays.asList(csvReader.nextToken().split(":"));
                } else {
                    csvReader = new StringTokenizer(line, ",");
                    //create new car type from first 5 fields
                    CarType type = new CarType(csvReader.nextToken(),
                            Integer.parseInt(csvReader.nextToken()),
                            Float.parseFloat(csvReader.nextToken()),
                            Double.parseDouble(csvReader.nextToken()),
                            Boolean.parseBoolean(csvReader.nextToken()));
                    //create N new cars with given type, where N is the 5th field
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
    
    static private class CrcData {
            public List<Car> cars = new LinkedList<Car>();
            public String name;
            public List<String> regions =  new LinkedList<String>();
    }
}