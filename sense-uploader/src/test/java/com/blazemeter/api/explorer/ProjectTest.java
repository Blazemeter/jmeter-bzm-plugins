package com.blazemeter.api.explorer;

import com.blazemeter.api.BlazeMeterReport;
import com.blazemeter.api.http.BlazeMeterHttpUtilsEmul;
import kg.apc.jmeter.reporters.notifier.StatusNotifierCallbackTest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ProjectTest {
    @org.junit.Test
    public void testFlow() throws Exception {
        StatusNotifierCallbackTest.StatusNotifierCallbackImpl notifier = new StatusNotifierCallbackTest.StatusNotifierCallbackImpl();
        BlazeMeterHttpUtilsEmul emul = new BlazeMeterHttpUtilsEmul(notifier, "test_address", "test_data_address", new BlazeMeterReport());

        JSONObject result = new JSONObject();
        result.put("id", "100");
        result.put("name", "NEW_TEST");
        JSONObject response = new JSONObject();
        response.put("result", result);

        Project project = new Project(emul, "10", "projectName");
        emul.addEmul(response);
        Test test = project.createTest("NEW_WORKSPACE");
        assertEquals("100", test.getId());
        assertEquals("NEW_TEST", test.getName());

        response.clear();
        JSONArray results = new JSONArray();
        results.add(result);
        results.add(result);
        response.put("result", results);
        emul.addEmul(response);

        List<Test> tests = project.getTests();
        assertEquals(2, tests.size());
        for (Test t :tests) {
            assertEquals("100", t.getId());
            assertEquals("NEW_TEST", t.getName());
        }
    }

    @org.junit.Test
    public void testFromJSON() throws Exception {
        StatusNotifierCallbackTest.StatusNotifierCallbackImpl notifier = new StatusNotifierCallbackTest.StatusNotifierCallbackImpl();
        BlazeMeterHttpUtilsEmul emul = new BlazeMeterHttpUtilsEmul(notifier, "test_address", "test_data_address", new BlazeMeterReport());
        JSONObject object = new JSONObject();
        object.put("id", "projectId");
        object.put("name", "projectName");
        Project project = Project.fromJSON(emul, object);
        assertEquals("projectId", project.getId());
        assertEquals("projectName", project.getName());
    }
}