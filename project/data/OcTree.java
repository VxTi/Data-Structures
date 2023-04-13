package project.data;

/*
 * Created on 13/04/2023 at 17:14
 * File created by Luca Warmenhoven
 */

import org.joml.Vector3d;
import java.util.ArrayList;
import java.util.List;

/** <tt>
 * Class for the data structured called an 'OcTree'.                           <br>
 * This data format can order data in a hierarchical way.                      <br>
 * This can drastically improve speed when used to make a lot of comparisons   <br>
 * in a 3 dimensional context.                                                 <br>
 * The OcTree contains, like the word might suggest, eight nodes per branch.   <br>
 * To create the simplest form of an OcTree, just call the default constructor <br>
 * 'new OcTree()' to create an OcTree with depth 1 and scale 1.                <br>
 * For more information, read the comments above the contained methods.
 * </tt>
 */
public class OcTree {

    private static final int OCTREE_SIZE = 8;
    private static final int MAX_DEPTH = 5;

    /**
     * Indices follow the rule: <tt>i = |x| + 4|y| + 2|z| </tt>                 <br>
     * with <tt>0 <= |x, y, z| <= 1</tt>                                        <br>
     * This means, if you want to get a branch at position <tt>[0.2, 0.7, 0.5]  <br>
     * i = |0.2| + 4|0.7| + 2|0.5|                                              <br>
     * i = 0     + 4(1)   + 2(1)                                                <br>
     * i = 6                         </tt>
     */
    private Branch root = new Branch(this, null, 0);
    private double scale = 1.0d;
    private int depth = 1;

    /**
     * Constructor for a default OcTree, with a coordinate scale of 1.0 and a depth of 1. <br>
     * This has a max node count of 2^(3 * 1) = 8.
     */
    public OcTree() {}

    /**
     * Constructor for an OcTree with a specified depth. <br>
     * This depth determines the maximum amount of nodes <br>
     * N = 2^(3 • D)                                     <br>
     * With a depth of say 3, this would mean N = 2^(3 • 3) = 512.
     */
    public OcTree(int depth) {
        this.depth = Math.max(Math.min(MAX_DEPTH, depth), 1);
    }

    /**
     * Constructor for an OcTree with a specified depth and scale.       <br>
     * Like the other constructor, this determines the maximum amount of <br>
     * nodes possible in the tree. It also contains a scale, which is   <br>
     * used in calculating the coordinates in the system.
     */
    public OcTree(int depth, double scale) {
        this(depth);
        this.scale = scale;
    }

    /**
     * Method for inserting 3 dimensional coordinates into the tree.           <br>
     * The method returns the leaf node associated with the given coordinates. <br>
     * @Note: 0 ≤ [x, y, z] ≤ scale, otherwise nothing will be added and no branch will be returned.
     */
    public Branch insert(double x, double y, double z) {
        Branch leaf = getLeaf(x, y, z);
        if (leaf == null)
            return null;

        if (leaf.data == null)
            leaf.data = new ArrayList<>();

        leaf.data.add(new Vector3d(x, y, z));

        return leaf;
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
    public Branch getLeaf(double x, double y, double z) {
        x /= scale;
        y /= scale;
        z /= scale;
        // We wouldn't want our values to be out of the range, do we now...
        if (x < 0 || x > 1 || y < 0 || y > 1 || z < 0 || z > 1)
            return null;

        Branch leaf = this.root;

        for (int i = 0, j; i < depth; i++) {
            // Indexing, as described at the top of the class file.
            j = FMath.round(x) + FMath.round(y) * 4 + FMath.round(z) * 2;

            if (leaf.branches == null)
                leaf.branches = new Branch[OCTREE_SIZE];

            if (leaf.branches[j] == null)
                leaf.branches[j] = new Branch(this, leaf, i + 1);

            // Branch down (maybe finally a leaf? :D )
            leaf = leaf.branches[j];

            // Scale down and scale up the coordinates to go down a layer in our tree (n = 2(n - 0.5|n|) => n = 2n - |n|)

            x = (2 * x - FMath.round(x));
            y = (2 * y - FMath.round(y));
            z = (2 * z - FMath.round(z));
        }
        return leaf;
    }


    /**
     * Method which recursively searches through the tree to find all of its leaves. <br>
     * This then returns the result
     */
    public List<Branch> getLeaves(Branch branchToSearch) {
        List<Branch> resultingBranches = new ArrayList<>();

        if (branchToSearch.isLeaf()) {
            resultingBranches.add(branchToSearch);
        } else {
            // Oh yes, recursion >:)
            for (int i = 0; i < OCTREE_SIZE && branchToSearch.branches != null; i++) {
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
    public List<Branch> getLeaves() {
        if (this.root.branches == null)
            return new ArrayList<>();

        return getLeaves(this.root);
    }

    /**
     * Method which returns the depth of this tree.
     */
    public int getDepth() { return this.depth; }

    public int getMaxNodeCount() {
        return OCTREE_SIZE << depth; // (1 << dimensions) << depth = 2^(dimensions * depth).
    }

    /**
     * Branch class
     */
    public static class Branch {
        public List<Vector3d> data = null;
        public Branch[] branches = null;
        public Branch parent;
        public OcTree root;
        public int depth;

        protected Branch(OcTree root, Branch parent, int depth) {
            this.parent = parent;
            this.depth = depth;
            this.root = root;
        }

        /** Checks whether the branch is the first in the series.
         *  An OcTreeBranch can both be a first branch and a leaf at the same time
         *  with [Depth = 1].
         **/
        public boolean isFirstBranch() {
            return this.parent == null;
        }

        /**
         * Checks whether the OcTreeBranch is just a regular branch
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
            return this.data != null && this.data.size() > 0;
        }
    }
}

