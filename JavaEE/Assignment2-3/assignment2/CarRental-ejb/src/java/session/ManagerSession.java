package session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import static javax.ejb.TransactionAttributeType.REQUIRED;
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
        return ((Long)em.createNamedQuery("getNumberOfReservationByCarType")
                .setParameter("companyName", company)
                .setParameter("carType", type)
                .getSingleResult()).intValue();
    }
    
    @TransactionAttribute(REQUIRED)
    @Override
    public void addRentalCompany(String name, List<String> regions) {
        CarRentalCompany company = new CarRentalCompany(name, regions, new LinkedList<Car>());
        em.persist(company);
    }
    
    @TransactionAttribute(REQUIRED)
    @Override
    public void addCar(String companyName, String carType) {
        CarRentalCompany company = em.find(CarRentalCompany.class, companyName);
        CarType ct = (CarType) em.createNamedQuery("getCarTypeByName")
                .setParameter("companyName", companyName)
                .setParameter("typeName", carType)
                .getSingleResult();
        Car car = new Car();
        car.setType(ct);
        car.setCompany(company);
        company.addCar(car);
    }
    
    @TransactionAttribute(REQUIRED)
    @Override
    public void addCarType(String companyName, CarType carType) {
        CarRentalCompany company = em.find(CarRentalCompany.class, companyName);
        company.addCarType(carType);
    }

    @Override
    public CarType getMostPopularCarType(String carRentalCompanyName, int year) {
        String carTypeName = (String) em.createNamedQuery("findMostPopularCarType")
                .setParameter("companyName", carRentalCompanyName)
                .setParameter("year", year)
                .setMaxResults(1)
                .getSingleResult();
        return (CarType) em.createNamedQuery("getCarTypeByName")
                .setParameter("companyName", carRentalCompanyName)
                .setParameter("typeName", carTypeName)
                .getSingleResult();
    }

    @Override
    public Set<String> getBestClients() {
        List<Object[]> results = em.createNamedQuery("findBestCostumers")
                .getResultList();
        Set<String> bestCostumers = new HashSet<String>();
        if (!results.isEmpty()) {
            boolean stop = false;
            int highestNumber = ((Number)results.get(0)[1]).intValue();
            Iterator<Object[]> iterator = results.iterator();
            while (!stop && iterator.hasNext()) {
                Object[] costumer = iterator.next();
                if (((Number)costumer[1]).intValue() == highestNumber) {
                    bestCostumers.add((String)costumer[0]);
                } else {
                    stop = true;
                }
            }
        }
        return bestCostumers;
    }
}