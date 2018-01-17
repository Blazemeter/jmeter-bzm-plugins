package com.blazemeter.api;

import com.blazemeter.api.data.JSONConverter;
import com.blazemeter.api.http.BlazeMeterHttpUtilsEmul;
import kg.apc.jmeter.http.HttpUtilsEmul;
import kg.apc.jmeter.reporters.notifier.StatusNotifierCallbackTest;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import org.apache.commons.io.FileUtils;
import org.apache.jmeter.samplers.SampleResult;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class BlazemeterAPIClientTest {

    @org.junit.Test
    public void testStartAnonTest() throws Exception {
        StatusNotifierCallbackTest.StatusNotifierCallbackImpl callback = new StatusNotifierCallbackTest.StatusNotifierCallbackImpl();
        BlazeMeterReport report = new BlazeMeterReport();
        HttpUtilsEmul httpUtils = new HttpUtilsEmul(callback, "https://a.blazemeter.com/", "https://a.blazemeter.com/");
        httpUtils.addEmul("{}");
        httpUtils.addEmul("{\"result\": {" +
                "\"test\": {\"id\": 1, \"name\": \"atest\"}, " +
                "\"master\": {\"id\": 1, \"name\": \"atest\"}, " +
                "\"session\": {\"id\": 1, \"name\": \"atest\", \"userId\": \"atest\"}, " +
                "\"publicTokenUrl\": \"http://\", " +
                "\"signature\": \"sign\"" +
                "}}");
        httpUtils.addEmul("{\"result\": {" +
                "\"session\": {\"id\": 1, \"name\": \"atest\", \"userId\": \"atest\", \"statusCode\": \"50\"} "+
                "}}");
        httpUtils.addEmul("{}");
        BlazeMeterAPIClient apiClient = new BlazeMeterAPIClient(httpUtils, callback, report);
        apiClient.prepare();
        assertEquals(report, apiClient.getReport());
        String link = apiClient.startOnline();
        System.out.println(link);
        assertFalse(link.isEmpty());
        List<SampleResult> sampleResults = generateResults();
        apiClient.sendOnlineData(JSONConverter.convertToJSON(sampleResults, sampleResults));
        apiClient.endOnline();
    }

    public static List<SampleResult> generateResults() {
        List<SampleResult> list = new LinkedList<>();
        list.add(new SampleResult(System.currentTimeMillis(), 1));
        list.add(new SampleResult(System.currentTimeMillis() + 1000, 1));
        list.add(new SampleResult(System.currentTimeMillis() + 2000, 1));
        list.add(new SampleResult(System.currentTimeMillis() + 3000, 1));

        SampleResult res = new SampleResult(System.currentTimeMillis() + 3000, 3);
        res.setSampleLabel("L2");
        list.add(res);

        res = new SampleResult(System.currentTimeMillis() + 3000, 3);
        res.setSampleLabel("L2");
        list.add(res);

        res = new SampleResult(System.currentTimeMillis() + 5000, 2);
        res.setSampleLabel("L2");
        list.add(res);

        res = new SampleResult(System.currentTimeMillis() + 4000, 3);
        res.setSampleLabel("L2");
        list.add(res);
        return list;
    }

    @org.junit.Test
    public void testUserFlow() throws Exception {

        URL url = BlazemeterAPIClientTest.class.getResource("/responses.json");
        JSONArray jsonArray = (JSONArray) JSONSerializer.toJSON(FileUtils.readFileToString(new File(url.getPath())), new JsonConfig());


        StatusNotifierCallbackTest.StatusNotifierCallbackImpl notifier = new StatusNotifierCallbackTest.StatusNotifierCallbackImpl();
        BlazeMeterReport report = new BlazeMeterReport();
        report.setShareTest(true);
        report.setProject("New project");
        report.setTitle("New test");
        report.setToken("123456");

        BlazeMeterHttpUtilsEmul emul = new BlazeMeterHttpUtilsEmul(notifier, "https://a.blazemeter.com/", "data_address", new BlazeMeterReport());
        for (Object resp : jsonArray) {
            emul.addEmul((JSON) resp);
        }
        BlazeMeterAPIClient apiClient = new BlazeMeterAPIClient(emul, notifier, report);
        apiClient.prepare();

        // if share user test
        String linkPublic = apiClient.startOnline();
        System.out.println(linkPublic);
        assertFalse(linkPublic.isEmpty());
        assertEquals("https://a.blazemeter.com//app/?public-token=public_test_token#/masters/master_id/summary", linkPublic);

        // if private user test
        report.setShareTest(false);
        String linkPrivate = apiClient.startOnline();
        System.out.println(linkPrivate);
        assertFalse(linkPrivate.isEmpty());
        assertEquals("https://a.blazemeter.com//app/#/masters/master_id", linkPrivate);


        List<SampleResult> sampleResults = generateResults();
        apiClient.sendOnlineData(JSONConverter.convertToJSON(sampleResults, sampleResults));
        apiClient.endOnline();
    }
}