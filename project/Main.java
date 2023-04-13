package project;

import project.data.QuadTree;
import project.data.Tree;

import java.util.List;

public class Main {

    public static Window window;

    public static void main(String[] args) {


        QuadTree<Object> tree = new QuadTree<>();
        tree.insert("hey", 0, 0.1);
        List<Tree.Branch<Object>> leaves = tree.getLeaves();

        System.out.printf("Found %d%s", leaves.size(), (leaves.size() == 1 ? " leaf" : " leaves"));

       /* window = WindowManager.createWindow("Hey", 600, 400);
        window.drawable = new Drawable3D(window);
        WindowManager.loadWindows();
        WindowManager.manage();*/
    }
}