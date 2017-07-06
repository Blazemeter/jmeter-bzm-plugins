package com.blazemeter.api.http;

import com.blazemeter.api.BlazeMeterReport;
import kg.apc.jmeter.reporters.notifier.StatusNotifierCallbackTest;
import net.sf.json.JSONObject;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;

import static org.junit.Assert.*;


public class BlazeMeterHttpUtilsTest {

    @Test
    public void testFlow() throws Exception {
        final StatusNotifierCallbackTest.StatusNotifierCallbackImpl callbackTest = new StatusNotifierCallbackTest.StatusNotifierCallbackImpl();
        final String address = "http://ip.jsontest.com/";
        BlazeMeterReport report = new BlazeMeterReport();
        report.setToken("test_token");
        BlazeMeterHttpUtils entity = new BlazeMeterHttpUtils(callbackTest, address, address, report);

        assertEquals(callbackTest, entity.getNotifier());
        assertEquals(address, entity.getAddress());
        assertEquals(address, entity.getDataAddress());
        assertEquals(report, entity.getReport());
        assertNotNull(entity.getHttpClient());

        HttpGet get = entity.createGet(address);
        JSONObject response = entity.queryObject(get, 200);
        assertEquals("test_token", get.getHeaders("X-Api-Key")[0].getValue());
        assertTrue(response.containsKey("ip"));

        report.setToken("test:token");
        entity = new BlazeMeterHttpUtils(callbackTest, address, address, report);
        HttpPost post = entity.createPost(address, "");
        entity.queryObject(post, 200);
        assertTrue(post.getHeaders("Authorization")[0].getValue().startsWith("Basic "));
        assertTrue(response.containsKey("ip"));
    }
}