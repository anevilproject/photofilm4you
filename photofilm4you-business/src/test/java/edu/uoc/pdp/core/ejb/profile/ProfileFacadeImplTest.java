package edu.uoc.pdp.core.ejb.profile;

import edu.uoc.pdp.core.dao.AdministratorDAO;
import edu.uoc.pdp.core.dao.CustomerDAO;
import edu.uoc.pdp.core.dao.RentDAO;
import edu.uoc.pdp.core.dao.UserDAO;
import edu.uoc.pdp.core.ejb.session.SessionManager;
import edu.uoc.pdp.db.entity.Administrator;
import edu.uoc.pdp.db.entity.Customer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProfileFacadeImplTest {

    @Mock
    private SessionManager sessionManager;
    @Mock
    private CustomerDAO customerDAO;
    @Mock
    private AdministratorDAO administratorDAO;
    @Mock
    private UserDAO userDAO;
    @Mock
    private RentDAO rentDAO;

    @InjectMocks
    private ProfileFacadeImpl profileFacade;


    @Test
    public void registerCustomerHashesPassword() {
        Customer customer = getCustomer("admin");

        profileFacade.registerCustomer(customer);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(userDAO, times(1)).save(customerCaptor.capture());

        assertSame(customer, customerCaptor.getValue());
        assertNotNull(customer.getDate());
        assertEquals("ISMvKXpXpadDiUoOSoAfww==", customerCaptor.getValue().getPassword());
    }

    @Test
    public void registerAdministratorHashesPassword() {
        Administrator administrator = new Administrator();
        administrator.setEmail("email@email.com");
        administrator.setPassword("admin");

        profileFacade.registerAdministrator(administrator);

        ArgumentCaptor<Administrator> administratorCaptor = ArgumentCaptor.forClass(Administrator.class);
        verify(userDAO, times(1)).save(administratorCaptor.capture());

        assertSame(administrator, administratorCaptor.getValue());
        assertNotNull(administrator.getDate());
        assertEquals("ISMvKXpXpadDiUoOSoAfww==", administratorCaptor.getValue().getPassword());
    }

    @Test
    public void updateCustomerKeepsOldPasswordWhenPasswordIsNull() {
        Customer customer = getCustomer(null);
        when(customerDAO.getById("customer_id")).thenReturn(getCustomer("ISMvKXpXpadDiUoOSoAfww=="));

        profileFacade.updateCustomerData(customer);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDAO, times(1)).save(customerCaptor.capture());

        assertSame(customer, customerCaptor.getValue());
        assertEquals("ISMvKXpXpadDiUoOSoAfww==", customerCaptor.getValue().getPassword());
    }

    @Test
    public void updateCustomerUpdatesPasswordWhenPresent() {
        Customer customer = getCustomer("admin2");

        profileFacade.updateCustomerData(customer);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDAO, times(1)).save(customerCaptor.capture());

        assertSame(customer, customerCaptor.getValue());
        assertEquals("yEJY6cOQWaiat32Ebdq5CQ==", customerCaptor.getValue().getPassword());
    }

    private Customer getCustomer(String password) {
        Customer customer = new Customer();
        customer.setId("customer_id");
        customer.setEmail("email@email.com");
        customer.setName("Name");
        customer.setPassword(password);
        return customer;
    }
}
