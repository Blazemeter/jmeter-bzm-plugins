package com.blazemeter.jmeter.controller.traverse;

import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.ThreadSafeCookieManager;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.collections.ListedHashTree;

import java.util.LinkedList;

public class CustomTreeCloner implements HashTreeTraverser {

    private final ListedHashTree newTree;

    private final LinkedList<Object> objects = new LinkedList<>();

    public CustomTreeCloner() {
        newTree = new ListedHashTree();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void addNode(Object node, HashTree subTree) {
        if (node instanceof CookieManager) {
            node = new ThreadSafeCookieManager((CookieManager) node);
        }
        newTree.add(objects, node);
        addLast(node);
    }


    /**
     * add node to objects LinkedList
     * @param node Object
     */
    private void addLast(Object node) {
        objects.addLast(node);
    }

    @Override
    public void subtractNode() {
        objects.removeLast();
    }

    public ListedHashTree getClonedTree() {
        return newTree;
    }

    @Override
    public void processPath() {
    }

}
