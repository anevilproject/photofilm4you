package edu.uoc.pdp.core.ejb.payment;

import edu.uoc.pdp.core.configuration.ConfigurationProperties;
import edu.uoc.pdp.core.model.payment.PaymentRedirect;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static edu.uoc.pdp.db.entity.UserRoles.CUSTOMER;

@Stateless
@RolesAllowed(CUSTOMER)
public class PaymentFacadeImpl implements PaymentFacade {

    @Inject
    private ConfigurationProperties properties;

    private Client client;

    @Override
    public String requestPayment(String rentId, BigDecimal amount) {
        try {
            WebTarget target = getClient().target(
                    UriBuilder.fromUri(properties.getPaymentUrl())
                            .queryParam("identifier", rentId)
                            .queryParam("amount", amount));
            PaymentRedirect redirect = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .put(Entity.json(null), PaymentRedirect.class);

            if (redirect != null && StringUtils.isNotBlank(redirect.getUrl())) {
                return redirect.getUrl();
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not get payment redirection", e);
        }
        throw new RuntimeException("Could not get payment redirection");
    }

    private Client getClient() {
        if (client == null) {
            client = ClientBuilder.newBuilder()
                    .connectTimeout(2000, TimeUnit.MILLISECONDS)
                    .readTimeout(30000, TimeUnit.MILLISECONDS)
                    .build();
        }
        return client;
    }
}
