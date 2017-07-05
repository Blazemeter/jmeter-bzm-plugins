package com.blazemeter.api.explorer.base;

import kg.apc.jmeter.reporters.StatusNotifierCallbackTest;
import net.sf.json.JSONObject;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HttpBaseEntityTest {

    @Test
    public void testFlow() throws Exception {
        final StatusNotifierCallbackTest.StatusNotifierCallbackImpl callbackTest = new StatusNotifierCallbackTest.StatusNotifierCallbackImpl();
        final String address = "http://ip.jsontest.com/";
        HttpBaseEntity entity = new HttpBaseEntity(callbackTest, address, address, "test_token", false);

        assertEquals(callbackTest, entity.getNotifier());
        assertEquals(address, entity.getAddress());
        assertEquals(address, entity.getDataAddress());
        assertEquals("test_token", entity.getToken());
        assertFalse(entity.isAnonymousTest());
        assertNotNull(entity.getHttpClient());

        HttpGet get = entity.createGet(address);
        JSONObject response = entity.queryObject(get, 200);
        assertEquals("test_token", get.getHeaders("X-Api-Key")[0].getValue());
        assertTrue(response.containsKey("ip"));

        entity = new HttpBaseEntity(callbackTest, address, address, "test:token", false);
        HttpPost post = entity.createPost(address, "");
        entity.queryObject(post, 200);
        assertTrue(post.getHeaders("Authorization")[0].getValue().startsWith("Basic "));
        assertTrue(response.containsKey("ip"));
    }
}