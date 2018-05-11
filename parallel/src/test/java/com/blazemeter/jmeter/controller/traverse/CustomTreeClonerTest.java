package com.blazemeter.jmeter.controller.traverse;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.ThroughputController;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.ThreadSafeCookieManager;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.junit.Test;

import static org.junit.Assert.*;

public class CustomTreeClonerTest {

    @Test
    public void testFlow() throws Exception {
        final CookieManager manager = new CookieManager();
        final ThroughputController controller = new ThroughputController();

        CustomTreeCloner cloner = new CustomTreeCloner();
        HashTree tree = createTestTree(controller, manager);
        tree.traverse(cloner);

        ListedHashTree clonedTree = cloner.getClonedTree();
        ListedHashTree loop = (ListedHashTree) clonedTree.values().toArray()[0];

        Object actualController = loop.keySet().toArray()[0];
        assertTrue("This links should be to the same instance", controller == actualController);

        Object actualManager = loop.get(actualController).keySet().toArray()[0];
        assertTrue("Cookie manager should be changed to ThreadSafe instance", actualManager instanceof ThreadSafeCookieManager);
    }

    private HashTree createTestTree(ThroughputController controller, ConfigTestElement configTestElement) {
        HashTree controllerNode = new HashTree();
        controllerNode.add(controller, configTestElement);


        LoopController loopController = new LoopController();
        HashTree loopNode = new HashTree();
        loopNode.add(loopController, controllerNode);

        HashTree hashTree = new HashTree();
        hashTree.add(loopNode);
        return hashTree;
    }
}