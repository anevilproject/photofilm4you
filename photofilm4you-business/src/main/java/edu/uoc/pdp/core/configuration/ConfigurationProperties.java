package edu.uoc.pdp.core.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.lang.management.ManagementFactory;

@Singleton
public class ConfigurationProperties {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationProperties.class);

    private static final String PORT = "jboss.as:socket-binding-group=standard-sockets,socket-binding=http";

    private static final String CANCELLATION_HOURS_FREE = "cancellation-hours-free";
    private static final String CANCELLATION_HOURS_PENALTY = "cancellation-hours-penalty";
    private static final String CANCELLATION_SOFT_PENALTY = "cancellation-soft-penalty";
    private static final String CANCELLATION_HARD_PENALTY = "cancellation-hard-penalty";
    private static final String MAX_IMAGE_WIDTH = "max-image-width";
    private static final String MAX_IMAGE_HEIGHT = "max-image-height";
    private static final String MAX_CATEGORY_DEPTH = "max-category-depth";
    private static final String PAYMENT_URL = "payment-url";

    /**
     * Returns the time X such that:
     * <p>
     * Time from cancellation to rent start >= X -> cancellation is free
     * Time from cancellation to rent start < X -> there can be a penalty
     */
    public Integer getCancellationHoursFree() {
        return getOrDefault(CANCELLATION_HOURS_FREE, 48);
    }

    /**
     * Returns the time X such that:
     * <p>
     * Time from cancellation to rent start >= X -> soft penalty is applied
     * Time from cancellation to rent start < X -> hard penalty is applied
     */
    public Integer getCancellationHoursPenalty() {
        return getOrDefault(CANCELLATION_HOURS_PENALTY, 24);
    }

    /**
     * Returns a penalty to be applied when the time from cancellation to rent start is between
     * CancellationHoursFree and CancellationHoursPenalty.
     * <p>
     * Penalties can either be a percentage or an absolute value.
     * <p>
     * Examples:
     * * 10.52 -> total value
     * * 10%   -> 10 percent
     */
    public String getCancellationSoftPenalty() {
        return getOrDefault(CANCELLATION_SOFT_PENALTY, "2");
    }

    /**
     * Returns a penalty to be applied when the time from cancellation to rent start is less than CancellationHoursPenalty.
     * <p>
     * Penalties can either be a percentage or an absolute value.
     * <p>
     * Examples:
     * * 10.52 -> total value
     * * 10%   -> 10 percent
     */
    public String getCancellationHardPenalty() {
        return getOrDefault(CANCELLATION_HARD_PENALTY, "25%");
    }

    /**
     * Returns the payment service url to be used when confirming a rent
     */
    public String getPaymentUrl() {
        return getOrDefault(PAYMENT_URL, "http://localhost:" + getServerPort() + "/photofilm4you-tpv/payment");
    }

    /**
     * Returns the maximum image width allowed
     */
    public int getMaxImageWidth() {
        return getOrDefault(MAX_IMAGE_WIDTH, 800);
    }

    /**
     * Returns the maximum image height allowed
     */
    public int getMaxImageHeight() {
        return getOrDefault(MAX_IMAGE_HEIGHT, 600);
    }

    /**
     * Returns the maximum category depth which limits how many nested categories can be created
     */
    public int getMaxCategoryDepth() {
        return getOrDefault(MAX_CATEGORY_DEPTH, 3);
    }

    public int getServerPort() {
        try {
            return Integer.parseInt(String.valueOf(getServer().getAttribute(new ObjectName(PORT), "port")));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return 8080;
    }

    @SuppressWarnings("unchecked")
    private <T> T getOrDefault(String property, T defaultValue) {
        T result = defaultValue;

        try {
            Context env = (Context) new InitialContext().lookup("java:app/env");

            result = (T) env.lookup(property);
        } catch (Exception e) {
            log.warn("Could not find a value for property {}, falling back to default value: {}", property, defaultValue);
        }
        return result;
    }

    private MBeanServer getServer() {
        return ManagementFactory.getPlatformMBeanServer();
    }
}
