import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class GeneralTree<T> implements TreeInterface<T> {
    private TreeNode<T> root;

    public GeneralTree() {
        root = null;
    }

    @Override
    public T getRootData() {
        return (root != null) ? root.data : null;
    }

    @Override
    public int getHeight() {
        return calculateHeight(root);
    }

    private int calculateHeight(TreeNode<T> node) {
        if (node == null) {
            return 0;
        }
        int maxHeight = 0;
        for (TreeNode<T> child : node.children) {
            maxHeight = Math.max(maxHeight, calculateHeight(child));
        }
        return maxHeight + 1;
    }

    @Override
    public int getNumberOfNodes() {
        return countNodes(root);
    }

    private int countNodes(TreeNode<T> node) {
        if (node == null) {
            return 0;
        }
        int count = 1;
        for (TreeNode<T> child : node.children) {
            count += countNodes(child);
        }
        return count;
    }

    @Override
    public boolean isEmpty() {
        return root == null;
    }

    @Override
    public void clear() {
        root = null;
    }

    public void setRoot(T rootData) {
        root = new TreeNode<>(rootData);
    }

    public TreeNode<T> getRootNode() {
        return root;
    }

    public void addChild(TreeNode<T> parent, T childData) {
        if (parent != null) {
            TreeNode<T> childNode = new TreeNode<>(childData);
            childNode.setParent(parent);
            parent.children.add(childNode);
        }
    }

    public Iterator<T> getPreorderIterator() {
        return new PreOrderTreeIterator<>(root);
    }


    public static class TreeNode<T> {
        private T data;
        private TreeNode<T> parent;
        private List<TreeNode<T>> children;

        public TreeNode(T data) {
            this.data = data;
            this.children = new ArrayList<>();
            this.parent = null;
        }

        public T getData() {
            return data;
        }

        public TreeNode<T> getParent() {
            return parent;
        }

        public void setParent(TreeNode<T> parent) {
            this.parent = parent;
        }

        public List<TreeNode<T>> getChildren() {
            return children;
        }
    }

    private static class PreOrderTreeIterator<T> implements Iterator<T> {
        private Stack<TreeNode<T>> stack = new Stack<>();

        public PreOrderTreeIterator(TreeNode<T> root) {
            if (root != null) {
                stack.push(root);
            }
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public T next() {
            if (!hasNext()) throw new IllegalStateException("No more elements in the tree.");
            TreeNode<T> current = stack.pop();
            for (int i = current.getChildren().size() - 1; i >= 0; i--) {
                stack.push(current.getChildren().get(i));
            }
            return current.getData();
        }
    }
}
