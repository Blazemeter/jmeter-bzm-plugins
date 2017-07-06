package com.blazemeter.api.explorer;

import com.blazemeter.api.BlazeMeterReport;
import com.blazemeter.api.http.BlazeMeterHttpUtilsEmul;
import kg.apc.jmeter.reporters.notifier.StatusNotifierCallbackTest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class WorkspaceTest {
    @org.junit.Test
    public void testFlow() throws Exception {
        StatusNotifierCallbackTest.StatusNotifierCallbackImpl notifier = new StatusNotifierCallbackTest.StatusNotifierCallbackImpl();
        BlazeMeterHttpUtilsEmul emul = new BlazeMeterHttpUtilsEmul(notifier, "test_address", "test_data_address", new BlazeMeterReport());

        JSONObject result = new JSONObject();
        result.put("id", "999");
        result.put("name", "NEW_PROJECT");
        JSONObject response = new JSONObject();
        response.put("result", result);

        Workspace workspace = new Workspace(emul, "888", "workspace_name");
        emul.addEmul(response);
        Project project = workspace.createProject("NEW_PROJECT");
        assertEquals("999", project.getId());
        assertEquals("NEW_PROJECT", project.getName());

        response.clear();
        JSONArray results = new JSONArray();
        results.add(result);
        results.add(result);
        response.put("result", results);
        emul.addEmul(response);

        List<Project> projects = workspace.getProjects();
        assertEquals(2, projects.size());
        for (Project p :projects) {
            assertEquals("999", p.getId());
            assertEquals("NEW_PROJECT", p.getName());
        }
    }

    @org.junit.Test
    public void testFromJSON() throws Exception {
        StatusNotifierCallbackTest.StatusNotifierCallbackImpl notifier = new StatusNotifierCallbackTest.StatusNotifierCallbackImpl();
        BlazeMeterHttpUtilsEmul emul = new BlazeMeterHttpUtilsEmul(notifier, "test_address", "test_data_address", new BlazeMeterReport());
        JSONObject object = new JSONObject();
        object.put("id", "workspaceId");
        object.put("name", "workspaceName");
        Workspace workspace = Workspace.fromJSON(emul, object);
        assertEquals("workspaceId", workspace.getId());
        assertEquals("workspaceName", workspace.getName());
    }
}