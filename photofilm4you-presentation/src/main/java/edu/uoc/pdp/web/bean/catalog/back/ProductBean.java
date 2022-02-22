package edu.uoc.pdp.web.bean.catalog.back;

import edu.uoc.pdp.core.ejb.catalog.CatalogFacade;
import edu.uoc.pdp.core.exception.ImageException;
import edu.uoc.pdp.core.exception.ProductException;
import edu.uoc.pdp.db.entity.Item;
import edu.uoc.pdp.db.entity.Product;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@ViewScoped
@ManagedBean(name = "productBean")
public class ProductBean extends AbstractCrudBean<Product> implements Serializable {

    private static final long serialVersionUID = -3754331031897437629L;

    @EJB
    private CatalogFacade catalogFacade;

    private List<Item> productItems;

    @Override
    protected Product fetch(String id) {
        return catalogFacade.getProduct(id);
    }

    @Override
    protected void delete(String id) {
        catalogFacade.deleteProduct(id);
    }

    @Override
    protected void persist(Product item) throws IOException, ImageException, ProductException {
        catalogFacade.saveProduct(item, getUploadedFile());
    }

    @Override
    protected List<Product> fetchAll() {
        return catalogFacade.listAllProducts();
    }

    @Override
    protected String getListView() {
        return "/back/product/listProducts.xhtml";
    }

    public List<SelectItem> getModelSelectItems() {
        return catalogFacade.listAllModels().stream()
                .filter(model -> model.getDeleted() == null || model.getId().equals(getItem().getModelId()))
                .map(model -> new SelectItem(model, model.getFullName()))
                .collect(Collectors.toList());
    }

    public String getItemRowClass() {
        return getRowClass(getProductItems());
    }

    public List<Item> getProductItems() {
        if (productItems == null) {
            productItems = catalogFacade.listItemsByProduct(getId());
        }
        return productItems;
    }

    public long countAvailableItems(String productId) {
        return catalogFacade.countAvailableItemsByProduct(productId);
    }

    public long countActiveItems(String productId) {
        return catalogFacade.countActiveItemsByProduct(productId);
    }
}
