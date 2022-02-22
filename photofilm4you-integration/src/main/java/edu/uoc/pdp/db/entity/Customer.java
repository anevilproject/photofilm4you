package edu.uoc.pdp.db.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

import static edu.uoc.pdp.db.entity.UserRoles.CUSTOMER;


@Entity
@DiscriminatorValue(value = CUSTOMER)
public class Customer extends User implements Serializable {

    private static final long serialVersionUID = 6323127995777641791L;

    @Pattern(
            regexp = "(X([-.])?0?\\d{7}([-.])?[A-Z]|[A-Z]([-.])?\\d{7}([-.])?[0-9A-Z]|\\d{8}([-.])?[A-Z])",
            message = "És necessari introduir un nif vàlid")
    private String nif;
    @NotBlank(message = "És necessari introduir el nom")
    @Size(max = 50, message = "El nom ha de tenir menys de 50")
    private String name;
    @NotBlank(message = "És necessari introduir els cognoms")
    @Size(max = 50, message = "Els cognoms han de tenir menys de 50")
    private String surname;
    @Pattern(
            regexp = "\\+?\\d{1,4}?[-.\\s]?\\(?\\d{1,3}?\\)?[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,9}",
            message = "És necessari introduir un telèfon vàlid")
    private String phone;
    @NotBlank(message = "És necessari introduir l'adreça")
    @Size(max = 100, message = "l'adreça ha de tenir menys de 100")
    private String address;
    @OneToMany(mappedBy = "customer")
    private List<Rent> rents;
    @OneToMany(mappedBy = "customer")
    private List<Question> questions;
    @OneToMany(mappedBy = "customer")
    private List<ProductRating> ratings;


    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Rent> getRents() {
        return rents;
    }

    public void setRents(List<Rent> rents) {
        this.rents = rents;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public List<ProductRating> getRatings() {
        return ratings;
    }

    public void setRatings(List<ProductRating> ratings) {
        this.ratings = ratings;
    }

    public String getFullName() {
        return name + " " + surname;
    }
}
