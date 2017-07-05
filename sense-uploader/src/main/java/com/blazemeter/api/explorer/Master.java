package com.blazemeter.api.explorer;

import kg.apc.jmeter.http.HttpUtils;
import net.sf.json.JSONObject;

import java.io.IOException;

public class Master extends BZAObject {

    public Master(HttpUtils httpUtils, String id, String name) {
        super(httpUtils, id, name);
    }

    /**
     * Makes a private user report public
     * @return public link to the report
     */
    public String makeReportPublic() throws IOException {
        String uri = httpUtils.getAddress() + String.format("/api/v4/masters/%s/public-token", getId());
        JSONObject obj = new JSONObject();
        obj.put("publicToken", "None");
        JSONObject response = httpUtils.queryObject(httpUtils.createPost(uri, obj.toString()), 201);

        return httpUtils.getAddress() + String.format("/app/?public-token=%s#/masters/%s/summary",
                extractPublicToken(response.getJSONObject("result")), getId());
    }

    private String extractPublicToken(JSONObject result) {
        return result.getString("publicToken");
    }

    public static Master fromJSON(HttpUtils httpUtils, JSONObject obj) {
        return new Master(httpUtils, obj.getString("id"), obj.getString("name"));
    }
}
