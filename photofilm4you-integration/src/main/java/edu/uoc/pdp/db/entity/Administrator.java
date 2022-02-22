package edu.uoc.pdp.db.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.Serializable;

import static edu.uoc.pdp.db.entity.UserRoles.ADMIN;

@Entity
@DiscriminatorValue(value = ADMIN)
public class Administrator extends User implements Serializable {

    private static final long serialVersionUID = -1760101235270139488L;
}
