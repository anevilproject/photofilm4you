package edu.uoc.pdp.web.bean.catalog.front;

import edu.uoc.pdp.core.ejb.catalog.CatalogFacade;
import edu.uoc.pdp.core.model.catalog.CategoryTree;
import edu.uoc.pdp.web.bean.BaseBean;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ViewScoped
@ManagedBean(name = "frontCategoriesBean")
public class FrontCategoriesBean extends BaseBean {

    private static final long serialVersionUID = 5444473867785289688L;

    @EJB
    private CatalogFacade catalogFacade;

    private CategoryTree categoryTree;

    /**
     * Builds a tree structure to render the active categories
     *
     * @return A category tree which may contain multiple root nodes
     */
    public CategoryTree getCategoryTree() {
        if (categoryTree == null) {
            categoryTree = catalogFacade.getActiveCategoryTree();
        }
        return categoryTree;
    }

    /**
     * Returns the full path to a category
     *
     * @param categoryId Leaf category id
     * @return The full path from the root category to the specified leaf
     */
    public String resolvePath(String categoryId) {
        String path = getCategoryTree().resolveNamePath(categoryId);

        if (StringUtils.isBlank(path)) {
            return "Tots";
        }
        return path;
    }
}
