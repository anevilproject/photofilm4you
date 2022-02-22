package edu.uoc.pdp.db.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "rent")
public class Rent implements Serializable, Identifiable, Rentable {

    private static final long serialVersionUID = 5227096644456543088L;

    @Id
    private String id;
    @Column(name = "date_from")
    private LocalDate from;
    @Column(name = "date_to")
    private LocalDate to;
    private LocalDateTime created;
    private BigDecimal totalPrice;
    @Enumerated(EnumType.STRING)
    private RentStatus status;
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "cancellation_id")
    private Cancellation cancellation;
    @ManyToMany
    @JoinTable(
            name = "rent_items",
            joinColumns = @JoinColumn(name = "rent_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<Item> items;


    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    @Override
    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }

    @Override
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public RentStatus getStatus() {
        return status;
    }

    public void setStatus(RentStatus status) {
        this.status = status;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public Cancellation getCancellation() {
        return cancellation;
    }

    public void setCancellation(Cancellation cancellation) {
        this.cancellation = cancellation;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public boolean isCancelled() {
        return cancellation != null;
    }

    public boolean isCancellable() {
        return !isCancelled() && !from.isBefore(LocalDate.now());
    }

    public Set<Product> getProducts() {
        if (hasItems()) {
            return items.stream().map(Item::getProduct).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    public Map<Product, List<Item>> getItemsByProduct() {
        if (hasItems()) {
            return items.stream().collect(Collectors.groupingBy(Item::getProduct));
        }
        return Collections.emptyMap();
    }

    public boolean hasItems() {
        return items != null && !items.isEmpty();
    }
}
