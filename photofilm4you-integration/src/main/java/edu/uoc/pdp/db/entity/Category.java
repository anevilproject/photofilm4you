package edu.uoc.pdp.db.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "category")
public class Category extends CatalogElement {

    private static final long serialVersionUID = 5912591392582979106L;

    @JoinColumn(name = "parentId")
    @OneToOne(fetch = FetchType.LAZY)
    private Category parent;
    @OneToMany(mappedBy = "category")
    private List<Product> products;

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public String getParentId() {
        return getParent() == null ? null : getParent().getId();
    }


}
