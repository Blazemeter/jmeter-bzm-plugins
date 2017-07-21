package com.blazemeter.api.explorer;

import com.blazemeter.api.BlazeMeterReport;
import com.blazemeter.api.http.BlazeMeterHttpUtilsEmul;
import kg.apc.jmeter.reporters.notifier.StatusNotifierCallbackTest;
import net.sf.json.JSONObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestTest {

    @org.junit.Test
    public void testFlow() throws Exception {
        StatusNotifierCallbackTest.StatusNotifierCallbackImpl notifier = new StatusNotifierCallbackTest.StatusNotifierCallbackImpl();
        BlazeMeterHttpUtilsEmul emul = new BlazeMeterHttpUtilsEmul(notifier, "test_address", "test_data_address", new BlazeMeterReport());

        JSONObject testResponse = new JSONObject();
        testResponse.put("id", "responseTestId");
        testResponse.put("name", "responseTestName");

        JSONObject sessionResponse = new JSONObject();
        sessionResponse.put("id", "responseSessionId");
        sessionResponse.put("name", "responseSessionName");
        sessionResponse.put("userId", "responseUserId");

        JSONObject masterResponse = new JSONObject();
        masterResponse.put("id", "responseMasterId");
        masterResponse.put("name", "responseMasterName");

        final String expectedURL = "localhost:7777/report";
        JSONObject result = new JSONObject();
        result.put("publicTokenUrl", expectedURL);
        result.put("test", testResponse);
        result.put("signature", "responseSignature");
        result.put("session", sessionResponse);
        result.put("master", masterResponse);

        JSONObject response = new JSONObject();
        response.put("result", result);

        Test test = new Test(emul, "testId", "testName");
        emul.addEmul(response);
        test.startExternal();
        assertEquals(1, emul.getRequests().size());
        assertEquals("", emul.getRequests().get(0));
        assertNull(test.getReportURL());
        checkTest(test, false);
        emul.clean();

        test = new Test(emul);

        emul.addEmul(response);
        String url = test.startAnonymousExternal();

        assertEquals(expectedURL, url);
        assertEquals(expectedURL, test.getReportURL());
        checkTest(test, true);
    }

    private void checkTest(Test test, boolean isStartAnonymous) {
        Master master = test.getMaster();
        assertEquals("responseMasterId", master.getId());
        assertEquals("responseMasterName", master.getName());

        Session session = test.getSession();
        assertEquals("responseSessionId", session.getId());
        assertEquals("responseSessionName", session.getName());
        assertEquals("responseUserId", session.getUserId());
        if (!isStartAnonymous) {
            assertEquals("testId", session.getTestId());
        } else {
            assertEquals("responseTestId", session.getTestId());
        }
        assertEquals("responseSignature", session.getSignature());

        assertEquals("responseSignature", test.getSignature());
    }

    @org.junit.Test
    public void testFromJSON() throws Exception {
        StatusNotifierCallbackTest.StatusNotifierCallbackImpl notifier = new StatusNotifierCallbackTest.StatusNotifierCallbackImpl();
        BlazeMeterHttpUtilsEmul emul = new BlazeMeterHttpUtilsEmul(notifier, "test_address", "test_data_address", new BlazeMeterReport());
        JSONObject object = new JSONObject();
        object.put("id", "testId");
        object.put("name", "testName");
        Test test = Test.fromJSON(emul, object);
        assertEquals("testId", test.getId());
        assertEquals("testName", test.getName());
    }
}