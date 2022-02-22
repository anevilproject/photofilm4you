package edu.uoc.pdp.core.model.availability;

public class ProductQuery {

    private final String category;
    private String brand;
    private String model;
    private String name;

    public ProductQuery(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
