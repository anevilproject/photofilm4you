package edu.uoc.pdp.db.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Entity
@DiscriminatorColumn(name = "role")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class User implements Serializable, Identifiable {

    private static final long serialVersionUID = -7301754737070621385L;

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    @Column(unique = true)
    @NotBlank(message = "És necessari introduir una adreça electrònica")
    @Email(message = "Adreça electrònica no vàlida")
    @Pattern(regexp = ".+@.+\\..+", message = "Adreça electrònica no vàlida")
    private String email;
    @NotBlank(message = "És necessari introduir una contrasenya")
    @Size(min = 4, max = 30, message = "La contrasenya ha de tenir 4 o més caràcters i menys de 30")
    private String password;
    @OneToMany(mappedBy = "user")
    private List<Response> responses;
    private LocalDate date;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Response> getResponses() {
        return responses;
    }

    public void setResponses(List<Response> responses) {
        this.responses = responses;
    }

    public boolean isAdmin() {
        return this instanceof Administrator;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
