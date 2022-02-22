package edu.uoc.pdp.web.converter;

import edu.uoc.pdp.db.entity.Category;

import javax.faces.convert.FacesConverter;

@FacesConverter(forClass = Category.class)
public class CategoryConverter extends IdentifiableConverter<Category> {

}
