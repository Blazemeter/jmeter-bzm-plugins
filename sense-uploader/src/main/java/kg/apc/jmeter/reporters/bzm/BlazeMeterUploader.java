package kg.apc.jmeter.reporters.bzm;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.gui.MainFrame;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.backend.BackendListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import kg.apc.jmeter.notifier.StatusNotifierCallback;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlazeMeterUploader extends BackendListener implements StatusNotifierCallback, Clearable {

    private static final Logger log = LoggingManager.getLoggerForClass();

    public static final String UPLOAD_TOKEN = "token";
    public static final String PROJECT = "project";
    public static final String TITLE = "title";
    public static final String SHARE_TEST = "share";

    protected BlazeMeterUploaderGui gui;
    private static volatile AtomicBoolean isTestStarted = new AtomicBoolean(false);

    public BlazeMeterUploader() {
        super();
        setClassname(JMeterUtils.getPropDefault("blazemeter.client", BlazeMeterBackendListenerClient.class.getName()));
        setProperty(TestElement.GUI_CLASS, BlazeMeterUploaderGui.class.getName());
    }

    @Override
    public void testStarted() {
        testStarted(MainFrame.LOCAL);
    }

    @Override
    public void testStarted(String host) {
        setArguments(createArguments());
        super.testStarted(host);
        if (!isTestStarted.getAndSet(true)) {
            initClient();
        }
    }

    private Arguments createArguments() {
        final Arguments arguments = new Arguments();
        arguments.addArgument(SHARE_TEST, Boolean.toString(isShareTest()));
        arguments.addArgument(PROJECT, getProject());
        arguments.addArgument(TITLE, getTitle());
        arguments.addArgument(UPLOAD_TOKEN, getUploadToken());
        return arguments;
    }

    @Override
    public void notifyAbout(String info) {
        if (gui != null) {
            gui.inform(info);
        }
        log.info(info);
    }

    @Override
    public Object clone() {
        BlazeMeterUploader clone = (BlazeMeterUploader) super.clone();
        clone.gui = this.gui;
        return clone;
    }

    @Override
    public void testEnded(String host) {
        super.testEnded(host);
        isTestStarted.set(false);
    }

    @Override
    public void testEnded() {
        testEnded(MainFrame.LOCAL);
    }

    public boolean isShareTest() {
        return getPropertyAsBoolean(SHARE_TEST);
    }

    public void setShareTest(boolean selected) {
        setProperty(SHARE_TEST, selected);
    }

    public void setProject(String proj) {
        setProperty(PROJECT, proj);
    }

    public String getProject() {
        return getPropertyAsString(PROJECT);
    }

    public void setTitle(String prefix) {
        setProperty(TITLE, prefix);
    }

    public String getTitle() {
        return getPropertyAsString(TITLE);
    }

    public void setUploadToken(String token) {
        setProperty(UPLOAD_TOKEN, token);
    }

    public String getUploadToken() {
        return getPropertyAsString(UPLOAD_TOKEN).trim();
    }

    public void setGui(BlazeMeterUploaderGui gui) {
        this.gui = gui;
    }

    // Inject StatusNotifierCallback (this) and resultCollector into private backendListenerClient
    // call initiateOnline()
    private void initClient() {
        try {
            Field listenerClientData = BlazeMeterUploader.class.getSuperclass().getDeclaredField("listenerClientData");
            listenerClientData.setAccessible(true);
            Object clientData = listenerClientData.get(this);
            Field clientField = clientData.getClass().getDeclaredField("client");
            clientField.setAccessible(true);
            BlazeMeterBackendListenerClient client = (BlazeMeterBackendListenerClient) clientField.get(clientData);
            client.setInformer(this);
            client.initiateOnline();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            log.error("Cannot inject links into backend listener client", e);
        }
    }

    // This is required so that
    // @see org.apache.jmeter.gui.tree.JMeterTreeModel.getNodesOfType()
    // can find the Clearable nodes - the userObject has to implement the interface.
    @Override
    public void clearData() {
    }
}
