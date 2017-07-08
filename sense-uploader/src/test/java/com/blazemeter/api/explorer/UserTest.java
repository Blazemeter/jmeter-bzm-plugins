package com.blazemeter.api.explorer;

import com.blazemeter.api.BlazeMeterReport;
import com.blazemeter.api.http.BlazeMeterHttpUtilsEmul;
import kg.apc.jmeter.reporters.notifier.StatusNotifierCallbackTest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class UserTest {

    @org.junit.Test
    public void testFlow() throws Exception {
        StatusNotifierCallbackTest.StatusNotifierCallbackImpl notifier = new StatusNotifierCallbackTest.StatusNotifierCallbackImpl();
        BlazeMeterReport report = new BlazeMeterReport();

        BlazeMeterHttpUtilsEmul emul = new BlazeMeterHttpUtilsEmul(notifier, "test_address", "test_data_address", report);

        User user = new User(emul);
        emul.addEmul(new JSONObject());
        user.ping();

        JSONObject acc = new JSONObject();
        acc.put("id", "accountId");
        acc.put("name", "accountName");
        JSONArray result = new JSONArray();
        result.add(acc);
        result.add(acc);
        JSONObject response = new JSONObject();
        response.put("result", result);
        emul.addEmul(response);

        List<Account> accounts = user.getAccounts();
        assertEquals(2, accounts.size());
        for (Account account : accounts) {
            assertEquals("accountId", account.getId());
            assertEquals("accountName", account.getName());
        }
    }
}