package com.blazemeter.api.explorer;

import kg.apc.jmeter.http.HttpUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Workspace extends BZAObject {

    public Workspace(HttpUtils httpUtils, String id, String name) {
        super(httpUtils, id, name);
    }

    /**
     * Create Project in current Workspace
     * @param name - Name of the new Project
     */
    public Project createProject(String name) throws IOException {
        String uri = httpUtils.getAddress() + "/api/v4/projects";
        JSONObject data = new JSONObject();
        data.put("name", name);
        data.put("workspaceId", Long.parseLong(getId()));
        JSONObject response = httpUtils.queryObject(httpUtils.createPost(uri, data.toString()), 201);
        return Project.fromJSON(httpUtils, response.getJSONObject("result"));
    }

    /**
     * @return list of Projects in current Workspace
     */
    public List<Project> getProjects() throws IOException {
        String uri = httpUtils.getAddress() + String.format("/api/v4/projects?workspaceId=%s&limit=99999", getId());
        JSONObject response = httpUtils.queryObject(httpUtils.createGet(uri), 200);
        return extractProjects(response.getJSONArray("result"));
    }

    private List<Project> extractProjects(JSONArray result) {
        List<Project> projects = new ArrayList<>();

        for (Object obj : result) {
            projects.add(Project.fromJSON(httpUtils, (JSONObject) obj));
        }

        return projects;
    }

    public static Workspace fromJSON(HttpUtils httpUtils, JSONObject obj) {
        return new Workspace(httpUtils, obj.getString("id"), obj.getString("name"));
    }
}
