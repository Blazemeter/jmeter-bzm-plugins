package com.blazemeter.api.explorer;

import kg.apc.jmeter.http.HttpUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Project extends BZAObject {

    public static final String DEFAULT_PROJECT = "Default project";

    public Project(HttpUtils httpUtils, String id, String name) {
        super(httpUtils, id, name);
    }

    /**
     * Create Test in current Project
     * @param name - title of the new Test
     */
    public Test createTest(String name) throws IOException {
        String uri = httpUtils.getAddress() + "/api/v4/tests";
        JSONObject data = new JSONObject();
        data.put("projectId", Long.parseLong(getId()));
        JSONObject configuration = new JSONObject();
        configuration.put("type", "external");
        data.put("configuration", configuration);
        data.put("name", name);
        JSONObject response = httpUtils.queryObject(httpUtils.createPost(uri, data.toString()), 201);
        return Test.fromJSON(httpUtils, response.getJSONObject("result"));
    }

    /**
     * @return list of Tests in current Project
     */
    public List<Test> getTests() throws IOException {
        String uri = httpUtils.getAddress() + "/api/v4/tests?projectId=" + getId();
        JSONObject response = httpUtils.queryObject(httpUtils.createGet(uri), 200);
        return extractTests(response.getJSONArray("result"));
    }

    private List<Test> extractTests(JSONArray result) {
        List<Test> accounts = new ArrayList<>();

        for (Object obj : result) {
            accounts.add(Test.fromJSON(httpUtils, (JSONObject) obj));
        }

        return accounts;
    }

    public static Project fromJSON(HttpUtils httpUtils, JSONObject obj) {
        return new Project(httpUtils, obj.getString("id"), obj.getString("name"));
    }
}
