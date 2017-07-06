package com.blazemeter.api.http;

import com.blazemeter.api.BlazeMeterReport;
import kg.apc.jmeter.http.HttpUtils;
import kg.apc.jmeter.notifier.StatusNotifierCallback;
import net.sf.json.JSON;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpRequestBase;


public class BlazeMeterHttpUtils extends HttpUtils {

    protected final BlazeMeterReport report;

    public BlazeMeterHttpUtils(StatusNotifierCallback notifier, String address, String dataAddress, BlazeMeterReport report) {
        super(notifier, address, dataAddress);
        this.report = report;
    }

    @Override
    protected void addRequiredHeader(HttpRequestBase httpRequestBase) {
        if (!report.isAnonymousTest()) {
            String token = report.getToken();
            if (token != null && token.contains(":")) {
                httpRequestBase.setHeader("Authorization", "Basic " + new String(Base64.encodeBase64(token.getBytes())));
            } else {
                httpRequestBase.setHeader("X-Api-Key", token);
            }
        }
    }

    @Override
    protected String extractErrorMessage(String response) {
        if (response != null && !response.isEmpty()) {
            try {
                JSON jsonResponse = JSONSerializer.toJSON(response, new JsonConfig());
                if (jsonResponse instanceof JSONObject) {
                    JSONObject object = (JSONObject) jsonResponse;
                    JSONObject errorObj = object.getJSONObject("error");
                    if (errorObj.containsKey("message")) {
                        return errorObj.getString("message");
                    }
                }
            } catch (JSONException ex) {
                log.debug("Cannot parse JSON error response: " + response);
            }
        }
        return response;
    }

    public BlazeMeterReport getReport() {
        return report;
    }
}
