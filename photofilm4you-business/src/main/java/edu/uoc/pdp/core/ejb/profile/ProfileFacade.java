package edu.uoc.pdp.core.ejb.profile;

import edu.uoc.pdp.db.entity.Administrator;
import edu.uoc.pdp.db.entity.Customer;
import edu.uoc.pdp.db.entity.Rent;

import javax.ejb.Local;
import java.util.List;

@Local
public interface ProfileFacade {

    /**
     * Creates a new customer user
     *
     * @param customer Customer to be registered
     */
    void registerCustomer(Customer customer);

    /**
     * Updates the customer information
     *
     * @param customer Customer to be updated
     */
    void updateCustomerData(Customer customer);

    /**
     * Creates a new administrator user
     *
     * @param administrator Administrator to be registered
     */
    void registerAdministrator(Administrator administrator);

    /**
     * Shows the customer's rental history report
     *
     * @return A list of rents
     */
    List<Rent> listMyRents();

    /**
     * Shows all active rents
     *
     * @return A list of rents
     */
    List<Rent> listAllActiveRents();

    /**
     * Gets a customer by email
     *
     * @return A Customer
     */
    Customer getCustomerByEmail(String email);

    /**
     * Finds out if a user is already registered
     *
     * @param email Email
     * @return {@code true} if the email is already registered, {@code false} otherwise
     */
    Boolean existUser(String email);

    /**
     * Retrieves all customers from the database.
     *
     * @return A list containing all the customers.
     */
    List<Customer> listCustomers();

    /**
     * Retrieves all administrators from the database.
     *
     * @return A list containing all the administrators.
     */
    List<Administrator> listAdministrators();
    
}