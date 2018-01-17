package kg.apc.jmeter.reporters.bzm;

import com.blazemeter.api.BlazeMeterAPIClient;
import com.blazemeter.api.BlazeMeterReport;
import com.blazemeter.api.data.JSONConverter;
import com.blazemeter.api.http.BlazeMeterHttpUtils;
import kg.apc.jmeter.JMeterPluginsUtils;
import net.sf.json.JSONObject;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.backend.BackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterStopTestException;
import org.apache.log.Logger;
import kg.apc.jmeter.notifier.StatusNotifierCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BlazeMeterBackendListenerClient implements BackendListenerClient {

    private static final Logger log = LoggingManager.getLoggerForClass();

    protected String address = JMeterUtils.getPropDefault("blazemeter.address", "https://a.blazemeter.com/");
    protected String dataAddress = JMeterUtils.getPropDefault("blazemeter.dataAddress", "https://data.blazemeter.com/");
    protected long delay = JMeterUtils.getPropDefault("blazemeter.delay", 5000);
    protected BlazeMeterAPIClient apiClient;
    protected BlazeMeterReport report;

    private boolean isInterruptedThroughUI;
    private final List<SampleResult> accumulator = new ArrayList<>();

    // this field set from BlazeMeterUploader after BackendListener created instance of this class
    protected StatusNotifierCallback informer;

    // BackendListener called this method when test was started
    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        init(context);
        accumulator.clear();
        isInterruptedThroughUI = false;
    }

    private void init(BackendListenerContext context) {
        report = new BlazeMeterReport();
        report.setShareTest(Boolean.valueOf(context.getParameter(BlazeMeterUploader.SHARE_TEST)));
        report.setProject(context.getParameter(BlazeMeterUploader.PROJECT));
        report.setTitle(context.getParameter(BlazeMeterUploader.TITLE));
        report.setToken(context.getParameter(BlazeMeterUploader.UPLOAD_TOKEN));
    }

    public void initiateOnline() {
        apiClient = new BlazeMeterAPIClient(
                new BlazeMeterHttpUtils(informer, address, dataAddress, report),
                informer, report);
        apiClient.prepare();
        try {
            log.info("Starting BlazeMeter test");
            String url = apiClient.startOnline();
            informer.notifyAbout("Started active test: <a href='" + url + "'>" + url + "</a>");
            try {
                JMeterPluginsUtils.openInBrowser(url);
            } catch (UnsupportedOperationException ex) {
                log.debug("Failed to open in browser", ex);
            }
        } catch (IOException ex) {
            informer.notifyAbout("Failed to start active test");
            log.warn("Failed to initiate active test", ex);
        }
    }

    @Override
    public void handleSampleResults(List<SampleResult> list, BackendListenerContext backendListenerContext) {
        if (isInterruptedThroughUI) {
            return;
        }

        accumulator.addAll(list);
        JSONObject data = JSONConverter.convertToJSON(accumulator, list);

        int counter = 0;
        while (!apiClient.isTestStarted() && counter < 3) {
            log.debug("Waiting for test starting");
            makeDelay();
            counter++;
        }

        try {
            apiClient.sendOnlineData(data);
        } catch (JMeterStopTestException ex) {
            isInterruptedThroughUI = true;
            StandardJMeterEngine.stopEngineNow();
        } catch (IOException e) {
            log.warn("Failed to send data: " + data, e);
        }

        makeDelay();
    }

    private void makeDelay() {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            log.warn("Backend listener client thread was interrupted");
        }
    }

    @Override
    public void teardownTest(BackendListenerContext backendListenerContext) throws Exception {
        if (!isInterruptedThroughUI) {
            apiClient.endOnline();
            informer.notifyAbout("Upload finished successfully");
        }
        accumulator.clear();
    }

    @Override
    public Arguments getDefaultParameters() {
        return null;
    }

    @Override
    public SampleResult createSampleResult(BackendListenerContext backendListenerContext, SampleResult sampleResult) {
        return sampleResult;
    }

    public BlazeMeterAPIClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(BlazeMeterAPIClient apiClient) {
        this.apiClient = apiClient;
    }

    public StatusNotifierCallback getInformer() {
        return informer;
    }

    public void setInformer(StatusNotifierCallback informer) {
        this.informer = informer;
    }

    public BlazeMeterReport getReport() {
        return report;
    }

    public void setReport(BlazeMeterReport report) {
        this.report = report;
    }
}
