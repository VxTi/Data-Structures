package project.data;

/**
 * Created on 13/04/2023 at 17:50
 * by Luca Warmenhoven.
 */
public class QuadTree<T> extends Tree<T> {

    public QuadTree(int depth, double scale) {
        super(2, depth, scale);
    }

    public QuadTree() {
        super(2, 1, 1);
    }
}
