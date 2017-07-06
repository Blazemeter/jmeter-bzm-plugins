package com.blazemeter.api.explorer;

import kg.apc.jmeter.http.HttpUtils;
import kg.apc.jmeter.reporters.notifier.StatusNotifierCallbackTest;

import static org.junit.Assert.*;

public class BZAObjectTest {

    @org.junit.Test
    public void test() throws Exception {
        HttpUtils httpUtils = new HttpUtils(new StatusNotifierCallbackTest.StatusNotifierCallbackImpl(), "", "");
        BZAObject entity = new BZAObject(httpUtils, "id", "name");
        assertEquals(httpUtils, entity.getHttpUtils());
        assertEquals("id", entity.getId());
        assertEquals("name", entity.getName());
        entity.setHttpUtils(null);
        entity.setId("id1");
        entity.setName("name1");
        assertNull(entity.getHttpUtils());
        assertEquals("id1", entity.getId());
        assertEquals("name1", entity.getName());
    }
}