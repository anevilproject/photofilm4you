package edu.uoc.pdp.tpv;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentStore {

    private static final PaymentStore INSTANCE = new PaymentStore();

    private final Map<String, Payment> payments = new ConcurrentHashMap<>();

    private PaymentStore() {
    }

    public static PaymentStore getInstance() {
        return INSTANCE;
    }

    public String registerPayment(Payment payment) {
        String id = payments.entrySet().stream()
                .filter(entry -> Objects.equals(entry.getValue().getIdentifier(), payment.getIdentifier()))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(UUID.randomUUID().toString());

        payments.put(id, payment);

        return id;
    }

    public Optional<Payment> getPayment(String id) {
        return StringUtils.isNotBlank(id) ? Optional.ofNullable(payments.get(id)) : Optional.empty();
    }

    public void removePayment(String id) {
        payments.remove(id);
    }
}
