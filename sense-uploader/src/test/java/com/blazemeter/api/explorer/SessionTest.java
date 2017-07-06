package com.blazemeter.api.explorer;

import com.blazemeter.api.BlazeMeterReport;
import com.blazemeter.api.http.BlazeMeterHttpUtilsEmul;
import kg.apc.jmeter.reporters.notifier.StatusNotifierCallbackTest;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;

import static org.junit.Assert.assertEquals;

public class SessionTest {

    @org.junit.Test
    public void testFlow() throws Exception {
        StatusNotifierCallbackTest.StatusNotifierCallbackImpl notifier = new StatusNotifierCallbackTest.StatusNotifierCallbackImpl();
        BlazeMeterHttpUtilsEmul emul = new BlazeMeterHttpUtilsEmul(notifier, "test_address", "test_data_address", new BlazeMeterReport());

        JSONObject data = new JSONObject();
        data.put("data", "Hello, World!");

        Session session = new Session(emul, "sessionId", "sessionName", "userId", "testId", "testSignature");

        emul.addEmul(JSONSerializer.toJSON("{\"result\":{\"session\":{\"statusCode\":15}}}", new JsonConfig()));
        session.sendData(data);
        assertEquals(1, emul.getRequests().size());
        assertEquals("{\"data\":\"Hello, World!\"}", emul.getRequests().get(0));
        emul.clean();

        emul.addEmul(new JSONObject());
        session.stop();
        assertEquals(1, emul.getRequests().size());
        assertEquals("", emul.getRequests().get(0));
        emul.clean();

        emul.addEmul(new JSONObject());
        session.stopAnonymous();
        assertEquals(1, emul.getRequests().size());
        assertEquals("{\"signature\":\"testSignature\",\"testId\":\"testId\",\"sessionId\":\"sessionId\"}", emul.getRequests().get(0));
        emul.clean();
    }

    @org.junit.Test
    public void testFromJSON() throws Exception {
        StatusNotifierCallbackTest.StatusNotifierCallbackImpl notifier = new StatusNotifierCallbackTest.StatusNotifierCallbackImpl();
        BlazeMeterHttpUtilsEmul emul = new BlazeMeterHttpUtilsEmul(notifier, "test_address", "test_data_address", new BlazeMeterReport());
        JSONObject object = new JSONObject();
        object.put("id", "sessionId");
        object.put("name", "sessionName");
        object.put("userId", "testUserId");
        Session session = Session.fromJSON(emul, "testId", "testSignature", object);
        assertEquals("sessionId", session.getId());
        assertEquals("sessionName", session.getName());
        assertEquals("testId", session.getTestId());
        assertEquals("testSignature", session.getSignature());
        assertEquals("testUserId", session.getUserId());
    }

}