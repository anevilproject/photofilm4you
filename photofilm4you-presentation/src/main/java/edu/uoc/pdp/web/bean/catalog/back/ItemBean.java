package edu.uoc.pdp.web.bean.catalog.back;

import edu.uoc.pdp.core.ejb.catalog.CatalogFacade;
import edu.uoc.pdp.core.exception.ItemException;
import edu.uoc.pdp.core.model.availability.ProductQuery;
import edu.uoc.pdp.db.entity.Item;
import edu.uoc.pdp.db.entity.ItemStatus;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@RequestScoped
@ManagedBean(name = "itemBean")
public class ItemBean extends AbstractCrudBean<Item> implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private CatalogFacade catalogFacade;

    @Override
    protected Item fetch(String id) {
        return catalogFacade.getItem(id);
    }


    @Override
    protected void delete(String id) {
        catalogFacade.deleteItem(id);
    }

    @Override
    protected void persist(Item item) throws ItemException {
        catalogFacade.saveItem(item);
    }

    @Override
    protected List<Item> fetchAll() {
        return catalogFacade.listAllItems();
    }

    @Override
    protected String getListView() {
        return "/back/item/listItems.xhtml";
    }

    public List<SelectItem> getAllProductSelectItem() {
        return catalogFacade.listActiveProducts(new ProductQuery(null)).stream()
                .map(product -> new SelectItem(product, product.getName()))
                .collect(Collectors.toList());
    }

    public ItemStatus[] getStatus() {
        return ItemStatus.values();
    }
}