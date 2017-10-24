package com.blazemeter.api;

import com.blazemeter.api.explorer.Account;
import com.blazemeter.api.explorer.Project;
import com.blazemeter.api.explorer.Test;
import com.blazemeter.api.explorer.User;
import com.blazemeter.api.explorer.Workspace;
import kg.apc.jmeter.http.HttpUtils;
import net.sf.json.JSONObject;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterStopTestException;
import kg.apc.jmeter.notifier.StatusNotifierCallback;
import org.apache.log.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BlazeMeterAPIClient {

    protected static final Logger log = LoggingManager.getLoggerForClass();

    private Test test;
    protected User user;
    private BlazeMeterReport report;
    private HttpUtils httpUtils;
    private StatusNotifierCallback notifier;

    public BlazeMeterAPIClient(HttpUtils httpUtils, StatusNotifierCallback notifier, BlazeMeterReport report) {
        this.httpUtils = httpUtils;
        this.notifier = notifier;
        this.report = report;
        this.user = new User(httpUtils);
    }

    public String startOnline() throws IOException {
        if (report.isAnonymousTest()) {
            notifier.notifyAbout("No BlazeMeter API key provided, will upload anonymously");
            test = new Test(httpUtils);
            return test.startAnonymousExternal();
        } else {
            test.startExternal();
            return (report.isShareTest()) ? test.getMaster().makeReportPublic() :
                    (httpUtils.getAddress() + "/app/#/masters/" + test.getMaster().getId());
        }
    }


    public boolean isTestStarted() {
        return test != null;
    }

    public void sendOnlineData(JSONObject data) throws IOException {
        JSONObject session = test.getSession().sendData(data);
        int statusCode = session.getInt("statusCode");
        if (statusCode > 100) {
            notifier.notifyAbout("Test was stopped through Web UI: " + session.getString("status"));
            throw new JMeterStopTestException("The test was interrupted through Web UI");
        }
    }

    public void endOnline() throws IOException {
        if (report.isAnonymousTest()) {
            test.getSession().stopAnonymous();
        } else {
            test.getSession().stop();
        }
        test = null;
    }

    public void prepare() {
        try {
            user.ping();
        } catch (IOException e) {
            notifier.notifyAbout("Cannot reach online results storage, maybe the address/token is wrong");
            return;
        }

        if (!report.isAnonymousTest()) {
            try {
                prepareClient(user);
            } catch (IOException e) {
                log.error("Cannot prepare client for sending report", e);
                throw new RuntimeException(e);
            }
        }
    }

    protected void prepareClient(User user) throws IOException {
        List<Account> accounts = user.getAccounts();
        List<Workspace> workspaces = findWorkspaces(accounts);
        if (!workspaces.isEmpty()) {
            Project project = findProject(workspaces);
            test = findTest(project);
        } else {
            notifier.notifyAbout("Your account has no active workspaces, please contact BlazeMeter support");
            // TODO: stop test?
        }
    }

    protected Test findTest(Project project) throws IOException {
        String testNameOrId = report.getTitle();
        if (testNameOrId == null || testNameOrId.isEmpty()) {
            testNameOrId = Test.DEFAULT_TEST;
            log.warn("Empty test title. Will be used '" + Test.DEFAULT_TEST + "' as test title");
        }

        final List<Test> tests = project.getTests();
        for (Test test : tests) {
            if (testNameOrId.equals(test.getId()) || testNameOrId.equals(test.getName())) {
                notifier.notifyAbout(String.format("Found BlazeMeter test: '%s' (id:%s)", test.getName(), test.getId()));
                return test;
            }
        }

        notifier.notifyAbout(String.format("Creating a test '%s' in '%s' project (id:%s)", testNameOrId, project.getName(), project.getId()));
        return project.createTest(testNameOrId);
    }

    protected Project findProject(List<Workspace> workspaces) throws IOException {
        String projectNameOrId = report.getProject();
        if (projectNameOrId == null || projectNameOrId.isEmpty()) {
            projectNameOrId = Project.DEFAULT_PROJECT;
            notifier.notifyAbout("Empty project name. Will be used '" + Project.DEFAULT_PROJECT + "' as project name");
        }


        for (Workspace workspace : workspaces) {
            final List<Project> projects = workspace.getProjects();
            for (Project project : projects) {
                if (projectNameOrId.equals(project.getId()) || projectNameOrId.equals(project.getName())) {
                    notifier.notifyAbout(String.format("Found BlazeMeter project: '%s' (id:%s)", project.getName(), project.getId()));
                    return project;
                }
            }
        }

        Workspace workspace = workspaces.get(0);
        notifier.notifyAbout(String.format("Creating a project '%s' in '%s' workspace (id:%s)", projectNameOrId, workspace.getName(), workspace.getId()));
        return workspace.createProject(projectNameOrId);
    }

    protected List<Workspace> findWorkspaces(List<Account> accounts) throws IOException {
        final List<Workspace> allWorkspaces = new ArrayList<>();
        for (Account account : accounts) {
            final List<Workspace> workspaces = account.getWorkspaces();
            if (!workspaces.isEmpty()) {
                allWorkspaces.addAll(workspaces);
            }
        }
        return allWorkspaces;
    }

    public BlazeMeterReport getReport() {
        return report;
    }
}

