package edu.uoc.pdp.web.bean.catalog.front;

import edu.uoc.pdp.core.ejb.catalog.CatalogFacade;
import edu.uoc.pdp.core.model.availability.ProductQuery;
import edu.uoc.pdp.db.entity.Brand;
import edu.uoc.pdp.db.entity.Model;
import edu.uoc.pdp.db.entity.Product;
import edu.uoc.pdp.web.bean.BaseBean;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ViewScoped
@ManagedBean(name = "frontProductsBean")
public class FrontProductsBean extends BaseBean {

    private static final long serialVersionUID = 4704015959305883556L;

    @EJB
    private CatalogFacade catalogFacade;

    private String category;
    private String model;
    private String brand;
    private String name;
    private List<Product> products;
    private List<Product> topProducts;
    private Set<Brand> brands;
    private Set<Model> models;

    /**
     * Returns the top 5 active products by rating
     *
     * @return A list with the 5 best reviewed products
     */
    public List<Product> getTopProducts() {
        if (topProducts == null) {
            topProducts = catalogFacade.listTopProducts(5);
        }
        return topProducts;
    }

    public List<Product> getProducts() {
        if (products == null) {
            ProductQuery query = new ProductQuery(category);
            query.setBrand(brand);
            query.setModel(model);
            query.setName(name);


            products = catalogFacade.listActiveProducts(query);
            products.sort(Comparator.comparing(Product::getDailyPrice));
        }
        return products;
    }

    public Set<Brand> getBrands() {
        if (brands == null) {
            brands = getProducts().stream().map(product -> product.getModel().getBrand()).collect(Collectors.toSet());
        }
        return brands;
    }

    public Set<Model> getModels() {
        if (models == null) {
            models = getProducts().stream().map(Product::getModel).collect(Collectors.toSet());
        }
        return models;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
