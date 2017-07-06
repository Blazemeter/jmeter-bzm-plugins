package com.blazemeter.api.http;

import com.blazemeter.api.BlazeMeterReport;
import kg.apc.jmeter.notifier.StatusNotifierCallback;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

public class BlazeMeterHttpUtilsEmul extends BlazeMeterHttpUtils {

    private LinkedList<JSON> responses = new LinkedList<>();
    private LinkedList<String> requests = new LinkedList<>();

    public BlazeMeterHttpUtilsEmul(StatusNotifierCallback notifier, String address, String dataAddress, BlazeMeterReport report) {
        super(notifier, address, dataAddress, report);
    }

    public void addEmul(JSON response) {
        responses.add(response);
    }

    public void clean() {
        requests.clear();
    }

    public LinkedList<String> getRequests() {
        return requests;
    }

    @Override
    public JSON query(HttpRequestBase request, int expectedCode) throws IOException {
        extractBody(request);
        return getResponse(request);
    }

    @Override
    public JSONObject queryObject(HttpRequestBase request, int expectedCode) throws IOException {
        extractBody(request);
        return (JSONObject) getResponse(request);
    }

    public void extractBody(HttpRequestBase request) throws IOException {
        if (request instanceof HttpPost) {
            HttpPost post = (HttpPost) request;
            InputStream inputStream = post.getEntity().getContent();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IOUtils.copy(inputStream, outputStream);
            requests.add(outputStream.toString());
        }
    }

    public JSON getResponse(HttpRequestBase request) throws IOException {
        log.info("Simulating request: " + request);
        if (responses.size() > 0) {
            JSON resp = responses.remove();
            log.info("Response: " + resp);
            return resp;
        } else {
            throw new IOException("No responses to emulate");
        }
    }
}
