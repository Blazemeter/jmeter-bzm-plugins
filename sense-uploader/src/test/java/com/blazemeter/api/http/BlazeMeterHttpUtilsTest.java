package com.blazemeter.api.http;

import com.blazemeter.api.BlazeMeterReport;
import kg.apc.jmeter.reporters.notifier.StatusNotifierCallbackTest;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;


public class BlazeMeterHttpUtilsTest {

    @Test
    public void testFlow() throws Exception {
        final StatusNotifierCallbackTest.StatusNotifierCallbackImpl callbackTest = new StatusNotifierCallbackTest.StatusNotifierCallbackImpl();
        final String address = "empty_host";
        BlazeMeterReport report = new BlazeMeterReport();
        report.setToken("test_token");
        BlazeMeterHttpUtils entity = createMockUtils(callbackTest, address, report);

        assertEquals(callbackTest, entity.getNotifier());
        assertEquals(address, entity.getAddress());
        assertEquals(address, entity.getDataAddress());
        assertEquals(report, entity.getReport());
        assertNotNull(entity.getHttpClient());

        HttpGet get = entity.createGet(address);
        JSONObject response = entity.queryObject(get, 200);
        assertEquals("test_token", get.getHeaders("X-Api-Key")[0].getValue());
        assertTrue(response.containsKey("message"));
        assertEquals("ok", response.get("message"));

        report.setToken("test:token");
        entity = createMockUtils(callbackTest, address, report);
        HttpPost post = entity.createPost(address, "");
        entity.queryObject(post, 200);
        assertTrue(post.getHeaders("Authorization")[0].getValue().startsWith("Basic "));
        assertTrue(response.containsKey("message"));
        assertEquals("ok", response.get("message"));
    }

    private BlazeMeterHttpUtils createMockUtils(StatusNotifierCallbackTest.StatusNotifierCallbackImpl callbackTest,
                                                String address, BlazeMeterReport report) {
        return new BlazeMeterHttpUtils(callbackTest, address, address, report) {
            @Override
            public JSON query(HttpRequestBase request, int expectedCode) throws IOException {
                try {
                    super.query(request, expectedCode);
                    fail("Cannot send request to " + address);
                } catch (IllegalStateException ex) {
                    assertEquals("Target host must not be null, or set in parameters.", ex.getMessage());
                }
                return JSONSerializer.toJSON("{\"message\" : \"ok\"}", new JsonConfig());
            }
        };
    }

    @Test
    public void extractErrorMessageTest() throws Exception {
        final StatusNotifierCallbackTest.StatusNotifierCallbackImpl callbackTest = new StatusNotifierCallbackTest.StatusNotifierCallbackImpl();
        final String address = "localhost";
        BlazeMeterReport report = new BlazeMeterReport();
        report.setToken("test_token");
        BlazeMeterHttpUtils entity = new BlazeMeterHttpUtils(callbackTest, address, address, report);

        String errorResponse = "{\"error\":{\"message\":\"Please, try later!\"}}";
        String message = entity.extractErrorMessage(errorResponse);

        assertEquals("Please, try later!", message);

        errorResponse = "Please, try later!";
        assertEquals(errorResponse, entity.extractErrorMessage(errorResponse));
    }
}