package edu.uoc.pdp.core.model.catalog;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CategoryTreeNode {

    private final String name;
    private final String id;
    private final CategoryTreeNode parent;
    private final List<CategoryTreeNode> children = new ArrayList<>();


    public CategoryTreeNode(String name, String id, CategoryTreeNode parent) {
        this.name = StringUtils.capitalize(StringUtils.lowerCase(name));
        this.id = id;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public CategoryTreeNode getParent() {
        return parent;
    }

    public List<CategoryTreeNode> getChildren() {
        return children;
    }

    public boolean isParent() {
        return !children.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CategoryTreeNode that = (CategoryTreeNode) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(id, that.id) &&
                Objects.equals(getParentId(), that.getParentId()) &&
                Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, getParentId(), children);
    }

    private String getParentId() {
        return parent == null ? null : parent.getId();
    }
}
