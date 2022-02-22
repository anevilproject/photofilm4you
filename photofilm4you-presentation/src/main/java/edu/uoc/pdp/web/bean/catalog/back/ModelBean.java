package edu.uoc.pdp.web.bean.catalog.back;

import edu.uoc.pdp.core.ejb.catalog.CatalogFacade;
import edu.uoc.pdp.core.exception.ModelException;
import edu.uoc.pdp.db.entity.Model;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@ViewScoped
@ManagedBean(name = "modelBean")
public class ModelBean extends AbstractCrudBean<Model> implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private CatalogFacade catalogFacade;

    @Override
    protected Model fetch(String id) {
        return catalogFacade.getModel(id);
    }

    @Override
    protected void delete(String id) {
        catalogFacade.deleteModel(id);
    }

    @Override
    protected void persist(Model item) throws ModelException {
        catalogFacade.saveModel(item);
    }

    @Override
    protected List<Model> fetchAll() {
        return catalogFacade.listAllModels();
    }

    @Override
    protected String getListView() {
        return "/back/model/listModels.xhtml";
    }

    public List<SelectItem> getBrandSelectItems() {
        return catalogFacade.listAllBrands().stream()
                .filter(brand -> brand.getDeleted() == null || brand.getId().equals(getItem().getBrandId()))
                .map(brand -> new SelectItem(brand, brand.getName()))
                .collect(Collectors.toList());
    }
}