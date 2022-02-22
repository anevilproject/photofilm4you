package edu.uoc.pdp.web.converter;

import edu.uoc.pdp.db.entity.Product;

import javax.faces.convert.FacesConverter;

@FacesConverter(forClass = Product.class)
public class ProductConverter extends IdentifiableConverter<Product> {

}
