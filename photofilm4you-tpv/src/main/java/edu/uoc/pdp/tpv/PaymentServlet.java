package edu.uoc.pdp.tpv;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

public class PaymentServlet extends HttpServlet {

    private static final long serialVersionUID = 6592870380907622894L;

    private final PaymentStore store = PaymentStore.getInstance();


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        Payment payment = store.getPayment(id).orElseThrow(() -> new RuntimeException("Invalid payment identifier!"));

        request.getSession().setAttribute("paymentId", id);
        request.getSession().setAttribute("amount", payment.getAmount());

        RequestDispatcher dispatcher = request.getRequestDispatcher("WEB-INF/jsp/payment.jsp");

        dispatcher.forward(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Payment payment = getPayment(request);
        String id = store.registerPayment(payment);
        String url = "http://localhost:" + request.getServerPort() + request.getContextPath() + "/payment?id=" + id;
        String result = "{\"url\": \"" + url + "\"}";

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.println(result);
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        String id = request.getParameter("id");
        Payment payment = store.getPayment(id).orElseThrow(() -> new RuntimeException("Invalid payment identifier!"));

        try {
            WebTarget target = getClient().target(UriBuilder.fromUri(getUrl(request))
                    .queryParam("identifier", payment.getIdentifier()));
            PaymentRedirect redirect = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.json(null), PaymentRedirect.class);

            if (redirect != null && StringUtils.isNotBlank(redirect.getUrl())) {
                store.removePayment(id);
                response.sendRedirect(redirect.getUrl());
            }
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong confirming the payment", e);
        }
    }

    private String getUrl(HttpServletRequest request) {
        return "http://localhost:" + request.getServerPort() + "/photofilm4you/api/rent/confirm";
    }

    private Client getClient() {
        return ClientBuilder.newBuilder()
                .connectTimeout(2000, TimeUnit.MILLISECONDS)
                .readTimeout(10000, TimeUnit.MILLISECONDS)
                .build();
    }

    private Payment getPayment(HttpServletRequest request) {
        BigDecimal amount = new BigDecimal(request.getParameter("amount"));
        String identifier = request.getParameter("identifier");

        if (identifier == null) {
            throw new RuntimeException("Expected transaction identifier");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Expected an amount greater than 0");
        }
        return new Payment(amount, identifier);
    }
}
