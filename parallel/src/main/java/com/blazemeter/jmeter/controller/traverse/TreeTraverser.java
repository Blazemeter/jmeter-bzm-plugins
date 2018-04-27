package com.blazemeter.jmeter.controller.traverse;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;

import java.util.LinkedList;

public class TreeTraverser implements HashTreeTraverser {

    private final LinkedList<ConfigTestElement> elements = new LinkedList<>();

    @Override
    public void addNode(Object node, HashTree hashTree) {
        if (node instanceof ConfigTestElement) {
            elements.add((ConfigTestElement) node);
        }
    }

    public LinkedList<ConfigTestElement> getElements() {
        return elements;
    }

    @Override
    public void subtractNode() {

    }

    @Override
    public void processPath() {

    }
}
