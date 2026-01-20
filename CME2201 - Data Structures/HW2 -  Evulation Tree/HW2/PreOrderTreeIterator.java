import java.util.Iterator;
import java.util.Stack;

public class PreOrderTreeIterator<T> implements Iterator<T> {
    private Stack<GeneralTree.TreeNode<T>> stack = new Stack<>();

    public PreOrderTreeIterator(GeneralTree.TreeNode<T> root) {
        if (root != null) stack.push(root);
    }

    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    @Override
    public T next() {
        if (!hasNext()) throw new IllegalStateException("No more elements in the tree.");
        GeneralTree.TreeNode<T> current = stack.pop();
        for (int i = current.getChildren().size() - 1; i >= 0; i--) {
            stack.push(current.getChildren().get(i));
        }
        return current.getData();
    }
}
