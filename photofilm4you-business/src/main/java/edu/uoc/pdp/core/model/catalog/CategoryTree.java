package edu.uoc.pdp.core.model.catalog;

import edu.uoc.pdp.db.entity.Category;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CategoryTree implements Serializable {

    private static final long serialVersionUID = -2165011042604252506L;

    private final List<CategoryTreeNode> root = new ArrayList<>();
    private final Map<String, CategoryTreeNode> categoriesById = new HashMap<>();
    private final List<Category> cycleCategories = new ArrayList<>();


    public CategoryTree(List<Category> categories) {
        addChildren(null, categories);
        categories.stream().filter(category -> !categoriesById.containsKey(category.getId())).forEach(cycleCategories::add);
    }

    public List<CategoryTreeNode> getRootNodes() {
        return root;
    }

    /**
     * Returns the full path to a category
     *
     * @param categoryId Leaf category id
     * @return The full path from the root category to the specified leaf
     */
    public String resolveNamePath(String categoryId) {
        List<CategoryTreeNode> branch = buildBranch(categoriesById.get(categoryId));

        if (branch.isEmpty()) {
            return "";
        }
        return branch.stream().map(CategoryTreeNode::getName).collect(Collectors.joining(" / "));
    }

    /**
     * Given a category id returns the depth from a root to the specified category
     *
     * @param leaf Category id
     * @return Depth of the category branch
     */
    public int getDepth(String leaf) {
        return buildBranch(categoriesById.get(leaf)).size();
    }

    /**
     * Returns wheter the category tree has a cycle or not
     *
     * @return {@code true} if the tree is cyclic, {@code false} otherwise
     */
    public boolean isCyclic() {
        return !cycleCategories.isEmpty();
    }

    /**
     * Returns a list of all categories that do not belong to any root node and therefore form at least one cycle
     *
     * @return List of cyclic categories
     */
    public List<Category> getCycleCategories() {
        return cycleCategories;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CategoryTree tree = (CategoryTree) o;
        return Objects.equals(root, tree.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }

    private void addChildren(CategoryTreeNode parent, List<Category> categories) {
        String parentId = parent == null ? null : parent.getId();
        List<CategoryTreeNode> list = parent == null ? root : parent.getChildren();

        for (Category category : categories) {
            if (Objects.equals(parentId, category.getParentId())) {
                CategoryTreeNode node = addCategory(category, list, parent);
                addChildren(node, categories);
            }
        }
    }

    private List<CategoryTreeNode> buildBranch(CategoryTreeNode node) {
        if (node == null) {
            return new ArrayList<>();
        }
        List<CategoryTreeNode> branch = buildBranch(node.getParent());
        branch.add(node);

        return branch;
    }

    private CategoryTreeNode addCategory(Category category, List<CategoryTreeNode> nodes, CategoryTreeNode parent) {
        CategoryTreeNode node = new CategoryTreeNode(category.getName(), category.getId(), parent);
        nodes.add(node);

        categoriesById.put(category.getId(), node);

        return node;
    }
}
