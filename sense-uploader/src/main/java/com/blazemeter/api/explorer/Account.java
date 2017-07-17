package com.blazemeter.api.explorer;

import kg.apc.jmeter.http.HttpUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Account extends BZAObject {

    public Account(HttpUtils httpUtils, String id, String name) {
        super(httpUtils, id, name);
    }

    /**
     * Create Workspace in current Account
     * @param name - Name of the new Workspace
     */
    public Workspace createWorkspace(String name) throws IOException {
        String uri = httpUtils.getAddress() + "/api/v4/workspaces";
        JSONObject data = new JSONObject();
        data.put("name", name);
        data.put("accountId", Long.parseLong(getId()));
        JSONObject response = httpUtils.queryObject(httpUtils.createPost(uri, data.toString()), 201);
        return Workspace.fromJSON(httpUtils, response.getJSONObject("result"));
    }

    /**
     * @return list of Workspace in current Account
     */
    public List<Workspace> getWorkspaces() throws IOException {
        String uri = httpUtils.getAddress() + String.format("/api/v4/workspaces?accountId=%s&enabled=true&limit=100", getId());
        JSONObject response = httpUtils.queryObject(httpUtils.createGet(uri), 200);
        return extractWorkspaces(response.getJSONArray("result"));
    }

    private List<Workspace> extractWorkspaces(JSONArray result) {
        List<Workspace> workspaces = new ArrayList<>();

        for (Object obj : result) {
            workspaces.add(Workspace.fromJSON(httpUtils, (JSONObject) obj));
        }

        return workspaces;
    }

    public static Account fromJSON(HttpUtils httpUtils, JSONObject obj) {
        return new Account(httpUtils, obj.getString("id"), obj.getString("name"));
    }
}
