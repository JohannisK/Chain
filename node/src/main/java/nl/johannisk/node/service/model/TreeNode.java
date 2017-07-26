package nl.johannisk.node.service.model;

import java.util.ArrayList;
import java.util.List;

public class TreeNode<T> {
    private T data = null;
    private List<TreeNode> children = new ArrayList<>();
    private TreeNode parent = null;
    private int depth;

    public TreeNode(T data) {
        this.data = data;
        depth = 0;
    }

    public TreeNode<T> addChild(T data) {
        TreeNode<T> newChild = new TreeNode<>(data);
        newChild.setParent(this);
        newChild.setDepth(depth + 1);
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

    private void setParent(TreeNode parent) {
        this.parent = parent;
    }

    private void setDepth(int depth) {
        this.depth = depth;
    }
}
