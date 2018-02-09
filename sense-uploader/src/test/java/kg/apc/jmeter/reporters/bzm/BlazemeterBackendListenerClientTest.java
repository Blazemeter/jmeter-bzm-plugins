package kg.apc.jmeter.reporters.bzm;

import com.blazemeter.api.BlazeMeterAPIClient;
import com.blazemeter.api.BlazemeterAPIClientTest;
import com.blazemeter.api.BlazeMeterReport;
import kg.apc.jmeter.http.HttpUtils;
import kg.apc.jmeter.reporters.notifier.StatusNotifierCallbackTest;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BlazemeterBackendListenerClientTest {

    @Test
    public void testGetAndSet() throws Exception {
        BlazeMeterBackendListenerClient client = new BlazeMeterBackendListenerClient();

        StatusNotifierCallbackTest.StatusNotifierCallbackImpl informer = new StatusNotifierCallbackTest.StatusNotifierCallbackImpl();
        client.setInformer(informer);
        assertEquals(informer, client.getInformer());
        HttpUtils httpUtils = new HttpUtils(informer, "http://a.blazemeter.com", "http://data.blazemeter.com");
        BlazeMeterReport report = new BlazeMeterReport();
        BlazeMeterAPIClient apiClient = new BlazeMeterAPIClient(httpUtils, informer, report);
        client.setApiClient(apiClient);
        assertEquals(apiClient, client.getApiClient());

        BlazeMeterReport report1 = new BlazeMeterReport();
        client.setReport(report1);
        assertEquals(report1, client.getReport());

        assertNull(client.getDefaultParameters());
        assertNull(client.createSampleResult(null, null));
    }

    @Test
    public void testFlow() throws Exception {
        StatusNotifierCallbackTest.StatusNotifierCallbackImpl notifier = new StatusNotifierCallbackTest.StatusNotifierCallbackImpl();
        BlazeMeterBackendListenerClient client = new BLCEmul();
        final Arguments arguments = new Arguments();
        arguments.addArgument(BlazeMeterUploader.SHARE_TEST, Boolean.toString(false));
        arguments.addArgument(BlazeMeterUploader.PROJECT, "project");
        arguments.addArgument(BlazeMeterUploader.TITLE, "title");
        client.setupTest(new BackendListenerContext(arguments));
        client.setInformer(notifier);
        client.initiateOnline();
        assertNotNull(client.getApiClient());

        client.handleSampleResults(BlazemeterAPIClientTest.generateResults(), null);

        client.teardownTest(null);

        String output = notifier.getBuffer().toString();
        assertTrue(output.contains("No BlazeMeter API key provided, will upload anonymously"));
    }
}