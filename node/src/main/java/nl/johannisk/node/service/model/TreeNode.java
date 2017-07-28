package nl.johannisk.node.service.model;

import java.util.ArrayList;
import java.util.List;

public class TreeNode<T> {
    private final T data;
    private final List<TreeNode> children = new ArrayList<>();
    private final TreeNode parent;
    private final int depth;

    public TreeNode(final TreeNode<T> parent, final T data, final int depth) {
        this.parent = parent;
        this.data = data;
        this.depth = depth;
    }

    public TreeNode<T> addChild(final T data) {
        TreeNode<T> newChild = new TreeNode<>(this, data, depth + 1);
        this.children.add(newChild);
        return newChild;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public TreeNode getParent() {
        return parent;
    }

    public T getData() {
        return data;
    }

    public int getDepth() {
        return depth;
    }
}
