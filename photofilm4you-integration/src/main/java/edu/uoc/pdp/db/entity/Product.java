package edu.uoc.pdp.db.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "product")
public class Product extends CatalogElement implements Deletable {

    private static final long serialVersionUID = 8620280137331422174L;

    @Size(max = 2000, message = "La descripció no pot tenir més de 2000 caràcters")
    @NotBlank(message = "La descripció no pot estar buida")
    private String description;
    @NotNull(message = "El preu no pot estar buit")
    @DecimalMin(value = "0.0", inclusive = false, message = "El preu ha de ser major que zero")
    @Digits(integer = 4, fraction = 2, message = "El preu ha de tenir dos decimals i màxim 4 digits enters")
    private BigDecimal dailyPrice;
    private Float rating;
    private LocalDateTime deleted;
    @ManyToOne
    @JoinColumn(name = "category_id")
    @NotNull(message = "La categoria no pot estar buida")
    private Category category;
    @OneToMany(mappedBy = "product")
    private List<Item> items;
    @ManyToOne()
    @JoinColumn(name = "model_id")
    @NotNull(message = "El model no pot estar buit")
    private Model model;
    @OneToMany(mappedBy = "product")
    private List<ProductRating> ratings;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getDailyPrice() {
        return dailyPrice;
    }

    public void setDailyPrice(BigDecimal dailyPrice) {
        this.dailyPrice = dailyPrice;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public List<ProductRating> getRatings() {
        return ratings;
    }

    public void setRatings(List<ProductRating> ratings) {
        this.ratings = ratings;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    @Override
    public LocalDateTime getDeleted() {
        return deleted;
    }

    @Override
    public void setDeleted(LocalDateTime deleted) {
        this.deleted = deleted;
    }

    @Override
    public List<? extends Deletable> getCascade() {
        return items;
    }

    @Override
    public Deletable getUpstream() {
        return getModel();
    }

    public String getModelId() {
        return model == null ? null : model.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }
}