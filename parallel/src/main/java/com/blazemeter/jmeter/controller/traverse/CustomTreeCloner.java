package com.blazemeter.jmeter.controller.traverse;

import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.ThreadSafeCookieManager;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.collections.ListedHashTree;

import java.util.LinkedList;

public class CustomTreeCloner implements HashTreeTraverser {

    private final ListedHashTree newTree;

    private final LinkedList<Object> objects = new LinkedList<>();

    private final boolean honourNoThreadClone;

    /**
     * Clone the test tree, honouring NoThreadClone markers.
     *
     */
    public CustomTreeCloner() {
        this(true);
    }

    /**
     * Clone the test tree.
     *
     * @param honourNoThreadClone set false to clone NoThreadClone nodes as well
     */
    public CustomTreeCloner(boolean honourNoThreadClone) {
        newTree = new ListedHashTree();
        this.honourNoThreadClone = honourNoThreadClone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void addNode(Object node, HashTree subTree) {
        if (node instanceof CookieManager) {
            node = new ThreadSafeCookieManager((CookieManager) node);
        }
        node = addNodeToTree(node);
        addLast(node);
    }

    /**
     * @param node Node to add to tree or not
     * @return Object node (clone or not)
     */
    protected Object addNodeToTree(Object node) {
        if ((node instanceof TestElement) // Check can cast for clone
                // Don't clone NoThreadClone unless honourNoThreadClone == false
                && (!honourNoThreadClone || !(node instanceof NoThreadClone))
                ) {
            node = ((TestElement) node).clone();
            newTree.add(objects, node);
        } else {
            newTree.add(objects, node);
        }
        return node;
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
