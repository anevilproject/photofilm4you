package edu.uoc.pdp.web.converter;

import com.google.common.reflect.TypeToken;
import edu.uoc.pdp.db.entity.Identifiable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

@SuppressWarnings({"unchecked", "UnstableApiUsage"})
public abstract class IdentifiableConverter<T extends Identifiable> implements Converter {

    private final Class<T> type;

    public IdentifiableConverter() {
        type = (Class<T>) new TypeToken<T>(getClass()) {
        }.getRawType();
    }


    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            try {
                T instance = type.newInstance();
                instance.setId(value);

                return instance;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            return type.cast(value).getId();
        }
        return null;
    }
}
