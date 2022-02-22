package edu.uoc.pdp.core.ejb.payment;

import javax.ejb.Local;
import java.math.BigDecimal;

@Local
public interface PaymentFacade {

    String requestPayment(String rentId, BigDecimal amount);
}
