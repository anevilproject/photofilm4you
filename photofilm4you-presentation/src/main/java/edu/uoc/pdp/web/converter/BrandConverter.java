package edu.uoc.pdp.web.converter;

import edu.uoc.pdp.db.entity.Brand;

import javax.faces.convert.FacesConverter;

@FacesConverter(forClass = Brand.class)
public class BrandConverter extends IdentifiableConverter<Brand> {

}
