package edu.uoc.pdp.api;

import com.google.common.collect.Sets;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Set;

@ApplicationPath("/api")
public class RestConfiguration extends Application {

    public Set<Class<?>> getClasses() {
        return Sets.newHashSet(RentConfirmationEndpoint.class);
    }
}
