package kg.apc.jmeter.reporters.bzm;

import com.blazemeter.api.BlazeMeterAPIClient;
import kg.apc.jmeter.http.HttpUtilsEmul;

import java.io.IOException;

public class BLCEmul extends BlazeMeterBackendListenerClient {
    @Override
    public void initiateOnline() {
        HttpUtilsEmul httpUtils = new HttpUtilsEmul(informer, "https://a.blazemeter.com/", "https://a.blazemeter.com/");
        httpUtils.addEmul("{}");
        httpUtils.addEmul("{\"result\": {" +
                "\"test\": {\"id\": 1, \"name\": \"atest\"}, " +
                "\"master\": {\"id\": 1, \"name\": \"atest\"}, " +
                "\"session\": {\"id\": 1, \"name\": \"atest\", \"userId\": \"atest\"}, " +
                "\"publicTokenUrl\": \"http://\", " +
                "\"signature\": \"sign\"" +
                "}}");
        httpUtils.addEmul("{\"result\": {" +
                "\"session\": {\"id\": 1, \"name\": \"atest\", \"userId\": \"atest\", \"statusCode\": \"50\"} " +
                "}}");
        httpUtils.addEmul("{}");
        apiClient = new BlazeMeterAPIClient(httpUtils, informer, report);
        apiClient.prepare();
        try {
            apiClient.startOnline();
        } catch (IOException e) {
            throw new RuntimeException("", e);
        }
    }
}
