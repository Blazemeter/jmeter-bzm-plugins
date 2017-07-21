package com.blazemeter.api.explorer;

import kg.apc.jmeter.http.HttpUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class User extends BZAObject {


    public User(HttpUtils httpUtils) {
        super(httpUtils, "", "");
    }

    /**
     * Quick check if we can access the service
     */
    public void ping() throws IOException {
        String uri = httpUtils.getAddress() + "/api/v4/web/version";
        httpUtils.query(httpUtils.createGet(uri), 200);
    }

    /**
     * @return list of Account for user token
     */
    public List<Account> getAccounts() throws IOException {
        String uri = httpUtils.getAddress()+ "/api/v4/accounts";
        JSONObject response = httpUtils.queryObject(httpUtils.createGet(uri), 200);
        return extractAccounts(response.getJSONArray("result"));
    }

    private List<Account> extractAccounts(JSONArray result) {
        List<Account> accounts = new ArrayList<>();

        for (Object obj : result) {
            accounts.add(Account.fromJSON(httpUtils, (JSONObject) obj));
        }

        return accounts;
    }
}
