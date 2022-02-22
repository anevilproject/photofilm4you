package edu.uoc.pdp.web.converter;

import edu.uoc.pdp.db.entity.Model;

import javax.faces.convert.FacesConverter;

@FacesConverter(forClass = Model.class)
public class ModelConverter extends IdentifiableConverter<Model> {

}
