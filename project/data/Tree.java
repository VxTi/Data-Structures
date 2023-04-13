package project.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created on 13/04/2023 at 17:14
 * by Luca Warmenhoven
 */
public abstract class Tree<T> {

    private double scale;
    private int nodeSize;
    private int dimensions;
    private int depth;
    protected Branch<T> root = new Branch<>(this, null, 0);

    public Tree(int dimensions, int depth, double scale) {
        this.nodeSize = (1 << dimensions) << depth;
        this.dimensions = dimensions;
        this.scale = scale;
        this.depth = depth;
    }

    /**
     * Method for inserting 3 dimensional coordinates into the tree.           <br>
     * The method returns the leaf node associated with the given coordinates. <br>
     * @Note: 0 ≤ [x, y, z] ≤ scale, otherwise nothing will be added and no branch will be returned.
     */
    public Branch<T> insert(NVector vec, T element) {
        if (vec.coordinates.length != this.dimensions)
            throw new IllegalArgumentException(String.format("Vector has different dimensions than required, Dv=%d, D=%d", vec.coordinates.length, dimensions));
        Branch<T> leaf = getLeaf(vec);
        if (leaf == null)
            return null;

        leaf.data = element;

        return leaf;
    }

    public Branch<T> insert(T element, double... coordinates) {
        return this.insert(new NVector(this, coordinates), element);
    }


    /**
     * Method for retrieving the leaf node of this OcTree, given the specified coordinates.<br>
     * This method will grow branches if they don't already exist, so beware. <br>
     * When in lack of <code>FMath.round(x)</code>, <br>
     * just use (int)(x + 0.5D) for speedier rounding.
     * @Parameters:  x, y, z
     * @Notice:     Method might return null with depth D = 0 <br>
     *              or with coordinates out of bounds.
     */
    public Branch<T> getLeaf(NVector vec) {
        int i, j, k;

        for (i = 0; i < dimensions; i++) {
            vec.coordinates[i] /= scale;
            if (vec.coordinates[i] < 0 || vec.coordinates[i] > 1) {
                System.err.print("Faulty given vector\n[");
                int len = 1;
                for (int n = 0; n < dimensions; n++) {
                    String append = String.format("%.3f%s", vec.coordinates[n], (n + 1 < dimensions ? ", " : ""));
                    System.err.printf(append);
                    if (n + 1 < dimensions)
                        len += append.length();
                }
                System.err.print("]\n");

                for (; len > 0; len--)
                    System.err.print(" ");
                System.err.printf("^\nCoordinate must be within range 0 - %.2f\n", scale);
                return null;
            }
        }

        Branch<T> leaf = this.root;

        for (i = 0; i < depth; i++) {
            // Indexing, as described at the top of the class file.
            for (j = 0, k = 0; k < dimensions; k++) {
                j += FMath.round(vec.coordinates[k]) * (1 << k);
                vec.coordinates[k] = (2 * vec.coordinates[k] - FMath.round(vec.coordinates[k]));
            }

            if (leaf.branches == null)
                leaf.branches = new Branch[nodeSize];

            if (leaf.branches[j] == null)
                leaf.branches[j] = new Branch<T>(this, leaf, i + 1);

            // Branch down (maybe finally a leaf? :D )
            leaf = leaf.branches[j];
        }
        return leaf;
    }


    /**
     * Method which recursively searches through the tree to find all of its leaves. <br>
     * This then returns the result
     */
    public List<Branch<T>> getLeaves(Branch<T> branchToSearch) {
        List<Branch<T>> resultingBranches = new ArrayList<>();

        if (branchToSearch.isLeaf()) {
            resultingBranches.add(branchToSearch);
        } else {
            // Oh yes, recursion >:)
            for (int i = 0; i < nodeSize && branchToSearch.branches != null; i++) {
                if (branchToSearch.branches[i] != null) {
                    resultingBranches.addAll(getLeaves(branchToSearch.branches[i]));
                }
            }
        }
        return resultingBranches;
    }

    /**
     * Method for retrieving all the leaves in the tree.
     */
    public List<Branch<T>> getLeaves() {
        if (this.root.branches == null)
            return new ArrayList<>();

        return getLeaves(this.root);
    }

    /**
     * Method which returns the depth of this tree.
     */
    public int getDepth() { return this.depth; }

    public int getMaxNodeCount() {
        return this.nodeSize; // (1 << dimensions) << depth = 2^(dimensions * depth).
    }

    public static class NVector {
        public double[] coordinates;
        public Tree<?> root;

        public NVector(Tree<?> root) {
            this.coordinates = new double[root.dimensions];
            this.root = root;
        }
        public NVector(Tree<?> root, double... coordinates) {
            if (coordinates.length != root.dimensions)
                throw new IllegalArgumentException("Coordinate length does not equal dimension size (D_required=" + root.dimensions + ", D_provided=" + coordinates.length + ")");
            this.coordinates = coordinates;
            this.root = root;
        }

        @Override
        public String toString() {
            return Arrays.toString(coordinates);
        }
    }

    /**
     * Branch class
     */
    public static class Branch<T> {
        public T data = null;
        public Tree.Branch<T>[] branches = null;
        public Tree.Branch<T> parent;
        public Tree<T> root;
        public int depth;

        protected Branch(Tree<T> root, Branch<T> parent, int depth) {
            this.parent = parent;
            this.depth = depth;
            this.root = root;
        }

        /** Checks whether the branch is the first in the series.
         *  A TreeBranch can both be a first branch and a leaf at the same time
         *  with [Depth = 1].
         **/
        public boolean isFirstBranch() {
            return this.parent == null;
        }

        /**
         * Checks whether the TreeBranch is just a regular branch
         * that doesn't contain any data.
         **/
        public boolean isEmptyBranch() {
            return this.data == null;
        }

        /**
         * Returns whether this branch is a leaf or not.
         * This is decided by whether it contains data,
         * since branches don't.
         */
        public boolean isLeaf() {
            return this.data != null;
        }
    }
}
