package edu.uoc.pdp.web.bean.catalog.back;

import edu.uoc.pdp.core.ejb.catalog.CatalogFacade;
import edu.uoc.pdp.core.exception.CategoryException;
import edu.uoc.pdp.db.entity.Category;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@ViewScoped
@ManagedBean(name = "categoryBean")
public class CategoryBean extends AbstractCrudBean<Category> implements Serializable {

    private static final long serialVersionUID = -4326823994759073390L;

    @EJB
    private CatalogFacade catalogFacade;

    @Override
    protected Category fetch(String id) {
        return catalogFacade.getCategory(id);
    }

    @Override
    protected void delete(String id) throws CategoryException {
        catalogFacade.deleteCategory(id);
    }

    @Override
    protected void persist(Category item) throws CategoryException {
        catalogFacade.saveCategory(item);
    }

    @Override
    protected List<Category> fetchAll() {
        return catalogFacade.listAllCategories();
    }

    public List<SelectItem> getAllCategorySelectItems() {
        return getAllItems().stream()
                .filter(category -> !category.getId().equals(getId()))
                .map(category -> new SelectItem(category, category.getName()))
                .collect(Collectors.toList());
    }

    @Override
    protected String getListView() {
        return "/back/category/listCategories.xhtml";
    }
}