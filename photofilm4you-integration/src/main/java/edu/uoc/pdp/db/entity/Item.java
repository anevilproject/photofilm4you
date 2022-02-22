package edu.uoc.pdp.db.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * JPA Class Item
 */
@Entity
@Table(name = "item")
public class Item implements Serializable, Deletable, Identifiable {

    private static final long serialVersionUID = -4236753218729042807L;

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Access(AccessType.PROPERTY)
    private String id;
    @NotBlank(message = "És necessari introduir un número de serie")
    private String serialNumber;
    @Enumerated(EnumType.STRING)
    @NotNull(message = "És necessari introduir un estat")
    private ItemStatus status;
    private LocalDateTime deleted;
    @ManyToOne()
    @JoinColumn(name = "product_id")
    @NotNull(message = "És necessari introduir un producte")
    private Product product;
    @ManyToMany(mappedBy = "items")
    private List<Rent> rents;
    @OneToMany(mappedBy = "item")
    private List<Reservation> reservations;


    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public List<Rent> getRents() {
        return rents;
    }

    public void setRents(List<Rent> rents) {
        this.rents = rents;
    }

    @Override
    public LocalDateTime getDeleted() {
        return deleted;
    }

    @Override
    public void setDeleted(LocalDateTime deleted) {
        this.deleted = deleted;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    @Override
    public Deletable getUpstream() {
        return getProduct();
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public String getProductId() {
        return (product != null ? getProduct().getId() : null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Item item = (Item) o;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
