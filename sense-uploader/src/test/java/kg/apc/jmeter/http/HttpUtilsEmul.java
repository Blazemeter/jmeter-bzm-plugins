package kg.apc.jmeter.http;

import kg.apc.jmeter.notifier.StatusNotifierCallback;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.IOException;
import java.util.LinkedList;

public class HttpUtilsEmul extends HttpUtils {

    private LinkedList<JSON> responses = new LinkedList<>();


    public HttpUtilsEmul(StatusNotifierCallback notifier, String address, String dataAddress) {
        super(notifier, address, dataAddress);
    }

    public void addEmul(JSON response) {
        responses.add(response);
    }

    @Override
    public JSON query(HttpRequestBase request, int expectedCode) throws IOException {
        return getResponse(request);
    }

    @Override
    public JSONObject queryObject(HttpRequestBase request, int expectedCode) throws IOException {
        return (JSONObject) getResponse(request);
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

    public void addEmul(String s) {
        addEmul(JSONSerializer.toJSON(s));
    }
}