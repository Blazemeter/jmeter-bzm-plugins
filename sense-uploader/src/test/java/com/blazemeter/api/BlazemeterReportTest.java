package com.blazemeter.api;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BlazemeterReportTest {

    @Test
    public void test() throws Exception {
        BlazeMeterReport report = new BlazeMeterReport();

        String project = "project";
        report.setProject(project);
        assertEquals(project, report.getProject());

        String title = "title";
        report.setTitle(title);
        assertEquals(title, report.getTitle());

        String token = "token";
        report.setToken(token);
        assertEquals(token, report.getToken());

        report.setShareTest(true);
        assertTrue(report.isShareTest());
    }
}