package com.blazemeter.api;

public class BlazeMeterReport {

    protected boolean isShareTest;
    protected String project;
    protected String title;
    protected String token;

    public boolean isAnonymousTest() {
        return (token == null || token.isEmpty());
    }

    public boolean isShareTest() {
        return isShareTest;
    }

    public void setShareTest(boolean shareTest) {
        isShareTest = shareTest;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
