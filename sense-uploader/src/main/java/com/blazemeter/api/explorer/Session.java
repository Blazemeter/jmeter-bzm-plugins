package com.blazemeter.api.explorer;

import kg.apc.jmeter.http.HttpUtils;
import net.sf.json.JSONObject;

import java.io.IOException;

public class Session extends BZAObject {

    private final String userId;
    private final String testId;
    private final String signature;

    public Session(HttpUtils httpUtils, String id, String name, String userId, String testId, String signature) {
        super(httpUtils, id, name);
        this.userId = userId;
        this.testId = testId;
        this.signature = signature;
    }

    /**
     * Send test json data for the report
     * @return session in JSONObject
     */
    public JSONObject sendData(JSONObject data) throws IOException {
        String uri = httpUtils.getDataAddress() +
                String.format("/submit.php?session_id=%s&signature=%s&test_id=%s&user_id=%s",
                        getId(), signature, testId, userId);
        uri += "&pq=0&target=labels_bulk&update=1"; //TODO: % self.kpi_target
        String dataStr = data.toString();
        log.debug("Sending active test data: " + dataStr);
        JSONObject response = httpUtils.queryObject(httpUtils.createPost(uri, dataStr), 200);
        return response.getJSONObject("result").getJSONObject("session");
    }

    /**
     * Stop session for user token
     */
    public void stop() throws IOException {
        String uri = httpUtils.getAddress() + String.format("/api/v4/sessions/%s/stop", getId());
        httpUtils.query(httpUtils.createPost(uri, ""), 202);
    }

    /**
     * Stop anonymous session
     */
    public void stopAnonymous() throws IOException {
        String uri = httpUtils.getAddress() + String.format("/api/v4/sessions/%s/terminate-external", getId());
        JSONObject data = new JSONObject();
        data.put("signature", signature);
        data.put("testId", testId);
        data.put("sessionId", getId());
        httpUtils.query(httpUtils.createPost(uri, data.toString()), 200);
    }

    public String getUserId() {
        return userId;
    }

    public String getTestId() {
        return testId;
    }

    public String getSignature() {
        return signature;
    }

    public static Session fromJSON(HttpUtils httpUtils, String testId, String signature, JSONObject session) {
        return new Session(httpUtils, session.getString("id"), session.getString("name"), session.getString("userId"), testId, signature);
    }
}
