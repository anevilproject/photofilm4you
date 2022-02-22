package edu.uoc.pdp.web.bean.catalog.back;

import edu.uoc.pdp.core.ejb.catalog.CatalogFacade;
import edu.uoc.pdp.core.exception.BrandException;
import edu.uoc.pdp.db.entity.Brand;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import java.io.Serializable;
import java.util.List;

@RequestScoped
@ManagedBean(name = "brandBean")
public class BrandBean extends AbstractCrudBean<Brand> implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private CatalogFacade catalogFacade;

    @Override
    protected Brand fetch(String id) {
        return catalogFacade.getBrand(id);
    }

    @Override
    protected void delete(String id) {
        catalogFacade.deleteBrand(id);
    }

    @Override
    protected void persist(Brand item) throws BrandException {
        catalogFacade.saveBrand(item);
    }

    @Override
    protected List<Brand> fetchAll() {
        return catalogFacade.listAllBrands();
    }

    @Override
    protected String getListView() {
        return "/back/brand/listBrands.xhtml";
    }
}