package edu.uoc.pdp.core.ejb.profile;

import edu.uoc.pdp.core.dao.AdministratorDAO;
import edu.uoc.pdp.core.dao.CustomerDAO;
import edu.uoc.pdp.core.dao.RentDAO;
import edu.uoc.pdp.core.dao.UserDAO;
import edu.uoc.pdp.core.ejb.session.SessionManager;
import edu.uoc.pdp.db.entity.Administrator;
import edu.uoc.pdp.db.entity.Customer;
import edu.uoc.pdp.db.entity.Rent;
import edu.uoc.pdp.db.entity.User;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

import static edu.uoc.pdp.db.entity.UserRoles.ADMIN;
import static edu.uoc.pdp.db.entity.UserRoles.CUSTOMER;

@Stateless
@PermitAll
public class ProfileFacadeImpl implements ProfileFacade {

    @Inject
    private SessionManager sessionManager;
    @Inject
    private CustomerDAO customerDAO;
    @Inject
    private AdministratorDAO administratorDAO;
    @Inject
    private UserDAO userDAO;
    @Inject
    private RentDAO rentDAO;


    /**
     * {@inheritDoc}
     */
    @Override
    public void registerCustomer(Customer customer) {
        registerUser(customer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed(CUSTOMER)
    public void updateCustomerData(Customer customer) {
        if (customer.getPassword() != null) {
            String pass = hash(customer.getPassword());
            customer.setPassword(pass);
        } else {
            Customer old = customerDAO.getById(customer.getId());
            customer.setPassword(old.getPassword());
        }
        customerDAO.save(customer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed(ADMIN)
    public void registerAdministrator(Administrator administrator) {
        registerUser(administrator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed(CUSTOMER)
    public List<Rent> listMyRents() {
        return rentDAO.findRentsByUser(sessionManager.getUserId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed(ADMIN)
    public List<Rent> listAllActiveRents() {
        return rentDAO.findActiveRents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed({ADMIN, CUSTOMER})
    public Customer getCustomerByEmail(String email) {
        return customerDAO.getByEmail(email);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean existUser(String email) {
        return userDAO.existUserByEmail(email);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed(ADMIN)
    public List<Customer> listCustomers() {
        return customerDAO.getAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RolesAllowed(ADMIN)
    public List<Administrator> listAdministrators() {
        return administratorDAO.getAll();
    }

    private void registerUser(User user) {
        user.setPassword(hash(user.getPassword()));
        user.setDate(LocalDate.now());

        userDAO.save(user);
    }

    private String hash(String pass) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(pass.getBytes(StandardCharsets.UTF_8));

            return new String(Base64.getEncoder().encode(md.digest()), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
