package com.blazemeter.api.explorer;

import com.blazemeter.api.BlazeMeterReport;
import com.blazemeter.api.http.BlazeMeterHttpUtilsEmul;
import kg.apc.jmeter.reporters.notifier.StatusNotifierCallbackTest;
import net.sf.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MasterTest {

    @Test
    public void testFlow() throws Exception {
        StatusNotifierCallbackTest.StatusNotifierCallbackImpl notifier = new StatusNotifierCallbackTest.StatusNotifierCallbackImpl();
        BlazeMeterHttpUtilsEmul emul = new BlazeMeterHttpUtilsEmul(notifier, "test_address", "test_data_address", new BlazeMeterReport());

        JSONObject result = new JSONObject();
        result.put("publicToken", "test_token");
        JSONObject response = new JSONObject();
        response.put("result", result);

        Master master = new Master(emul, "master_id", "master_name");
        emul.addEmul(response);
        String url = master.makeReportPublic();
        assertEquals("test_address/app/?public-token=test_token#/masters/master_id/summary", url);
    }

    @Test
    public void testFromJSON() throws Exception {
        StatusNotifierCallbackTest.StatusNotifierCallbackImpl notifier = new StatusNotifierCallbackTest.StatusNotifierCallbackImpl();
        BlazeMeterHttpUtilsEmul emul = new BlazeMeterHttpUtilsEmul(notifier, "test_address", "test_data_address", new BlazeMeterReport());
        JSONObject object = new JSONObject();
        object.put("id", "masterId");
        object.put("name", "masterName");
        Master master = Master.fromJSON(emul, object);
        assertEquals("masterId", master.getId());
        assertEquals("masterName", master.getName());
    }
}