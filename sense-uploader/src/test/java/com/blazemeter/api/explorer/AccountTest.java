package com.blazemeter.api.explorer;

import com.blazemeter.api.BlazeMeterReport;
import com.blazemeter.api.http.BlazeMeterHttpUtilsEmul;
import kg.apc.jmeter.reporters.notifier.StatusNotifierCallbackTest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class AccountTest {

    @Test
    public void testFlow() throws Exception {
        StatusNotifierCallbackTest.StatusNotifierCallbackImpl notifier = new StatusNotifierCallbackTest.StatusNotifierCallbackImpl();
        BlazeMeterHttpUtilsEmul emul = new BlazeMeterHttpUtilsEmul(notifier, "test_address", "test_data_address", new BlazeMeterReport());

        JSONObject result = new JSONObject();
        result.put("id", "100");
        result.put("name", "NEW_WORKSPACE");
        JSONObject response = new JSONObject();
        response.put("result", result);

        Account account = new Account(emul, "777", "account_name");
        emul.addEmul(response);
        Workspace workspace = account.createWorkspace("NEW_WORKSPACE");
        assertEquals("100", workspace.getId());
        assertEquals("NEW_WORKSPACE", workspace.getName());

        response.clear();
        JSONArray results = new JSONArray();
        results.add(result);
        results.add(result);
        response.put("result", results);
        emul.addEmul(response);

        List<Workspace> workspaces = account.getWorkspaces();
        assertEquals(2, workspaces.size());
        for (Workspace wsp :workspaces) {
            assertEquals("100", wsp.getId());
            assertEquals("NEW_WORKSPACE", wsp.getName());
        }
    }

    @Test
    public void testFromJSON() throws Exception {
        StatusNotifierCallbackTest.StatusNotifierCallbackImpl notifier = new StatusNotifierCallbackTest.StatusNotifierCallbackImpl();
        BlazeMeterHttpUtilsEmul emul = new BlazeMeterHttpUtilsEmul(notifier, "test_address", "test_data_address", new BlazeMeterReport());
        JSONObject object = new JSONObject();
        object.put("id", "accountId");
        object.put("name", "accountName");
        Account account = Account.fromJSON(emul, object);
        assertEquals("accountId", account.getId());
        assertEquals("accountName", account.getName());
    }

}