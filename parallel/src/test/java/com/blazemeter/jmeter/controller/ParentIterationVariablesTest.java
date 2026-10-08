package com.blazemeter.jmeter.controller;

import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ParentIterationVariablesTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        TestJMeterUtils.createJmeterEnv();
    }

    @Test
    public void delegatesStringAndObjectVariablesToParent() {
        JMeterVariables parent = new JMeterVariables();
        parent.put("token", "abc");
        ParallelSampler.ParentIterationVariables child = new ParallelSampler.ParentIterationVariables(parent);

        assertEquals("abc", child.get("token"));

        child.put("token", "def");
        assertEquals("def", parent.get("token"));

        // Loop controllers store __jm__*__idx with putObject. Expressions read it with get().
        child.putObject("__jm__renderLoop__idx", "5");
        assertEquals("5", child.get("__jm__renderLoop__idx"));
        assertEquals("5", parent.get("__jm__renderLoop__idx"));

        child.putObject("connection", Integer.valueOf(7));
        assertEquals(Integer.valueOf(7), child.getObject("connection"));
        assertEquals(Integer.valueOf(7), parent.getObject("connection"));
    }

    @Test
    public void removeAndPutAllUpdateParent() {
        JMeterVariables parent = new JMeterVariables();
        parent.put("keep", "yes");
        parent.put("drop", "gone");
        ParallelSampler.ParentIterationVariables child = new ParallelSampler.ParentIterationVariables(parent);

        assertEquals("gone", child.remove("drop"));
        assertNull(parent.get("drop"));
        assertNull(child.get("drop"));

        Map<String, String> extra = new HashMap<String, String>();
        extra.put("added", "1");
        child.putAll(extra);
        assertEquals("1", parent.get("added"));

        JMeterVariables other = new JMeterVariables();
        other.put("fromOther", "2");
        child.putAll(other);
        assertEquals("2", parent.get("fromOther"));
        assertEquals("yes", child.get("keep"));
    }

    @Test
    public void iterationReadFollowsParentAndIncDoesNotAdvanceIt() {
        JMeterVariables parent = new JMeterVariables();
        parent.incIteration();
        parent.incIteration();
        ParallelSampler.ParentIterationVariables child = new ParallelSampler.ParentIterationVariables(parent);

        assertEquals(2, child.getIteration());
        child.incIteration();
        assertEquals(2, parent.getIteration());
        assertEquals(2, child.getIteration());
    }

    @Test
    public void entrySetAndSameUserFlagFollowParent() {
        JMeterVariables parent = new JMeterVariables();
        ParallelSampler.ParentIterationVariables child = new ParallelSampler.ParentIterationVariables(parent);
        child.put("visible", "1");
        assertTrue(containsKey(child, "visible"));
        assertTrue(containsKey(parent, "visible"));

        assertFalse(child.isSameUserOnNextIteration());
        child.putObject("__jmv_SAME_USER", Boolean.TRUE);
        assertTrue(child.isSameUserOnNextIteration());
        assertEquals(Boolean.TRUE, parent.getObject("__jmv_SAME_USER"));
    }

    private static boolean containsKey(JMeterVariables variables, String key) {
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            if (key.equals(entry.getKey())) {
                return true;
            }
        }
        return false;
    }
}
