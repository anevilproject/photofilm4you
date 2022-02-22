package edu.uoc.pdp.api;

import edu.uoc.pdp.core.ejb.rent.RentFacade;
import edu.uoc.pdp.core.model.payment.PaymentRedirect;

import javax.annotation.ManagedBean;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@ManagedBean
@Path("/rent")
@RequestScoped
public class RentConfirmationEndpoint {

    private static final String SUCCESS_PAGE = "/customer/rent/showRent.xhtml?rentId=";

    @Inject
    private RentFacade rentFacade;
    @Context
    private HttpServletRequest servletRequest;

    @POST
    @Path("/confirm")
    @Produces(APPLICATION_JSON)
    public PaymentRedirect confirm(@QueryParam("identifier") String rentId) {
        rentFacade.confirmPayment(rentId);

        PaymentRedirect redirect = new PaymentRedirect();
        redirect.setUrl(getBaseUrl() + SUCCESS_PAGE + rentId);

        return redirect;
    }

    private String getBaseUrl() {
        return "http://localhost:" + servletRequest.getServerPort() + servletRequest.getContextPath();
    }
}
