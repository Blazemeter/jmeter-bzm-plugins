package com.blazemeter.api;

import com.blazemeter.api.explorer.Account;
import com.blazemeter.api.explorer.Project;
import com.blazemeter.api.explorer.Test;
import com.blazemeter.api.explorer.User;
import com.blazemeter.api.explorer.Workspace;
import com.blazemeter.api.explorer.base.HttpBaseEntity;
import net.sf.json.JSONObject;
import org.apache.jorphan.util.JMeterStopTestException;
import kg.apc.jmeter.reporters.StatusNotifierCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BlazemeterAPIClient extends HttpBaseEntity {


    private Test test;
    protected User user;
    private BlazemeterReport report;

    public BlazemeterAPIClient(StatusNotifierCallback notifier, String address, String dataAddress, BlazemeterReport report) {
        super(notifier, address, dataAddress, report.getToken(), report.isAnonymousTest());
        this.report = report;
        this.user = new User(this);
    }

    public String startOnline() throws IOException {
        if (isAnonymousTest()) {
            notifier.notifyAbout("No BlazeMeter API key provided, will upload anonymously");
            test = new Test(this);
            return test.startAnonymousExternal();
        } else {
            test.startExternal();
            return (report.isShareTest()) ? test.getMaster().makeReportPublic() :
                    (address + "/app/#/masters/" + test.getMaster().getId());
        }
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
        if (isAnonymousTest()) {
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

        if (!isAnonymousTest()) {
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

    public BlazemeterReport getReport() {
        return report;
    }
}

