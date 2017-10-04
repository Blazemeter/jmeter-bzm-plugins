package kg.apc.jmeter.reporters.bzm;

import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BlazemeterUploaderTest {

    @BeforeClass
    public static void setUpClass()
            throws Exception {
        TestJMeterUtils.createJmeterEnv();
    }

    @After
    public void tearDown() {
        JMeterUtils.getJMeterProperties().remove("blazemeter.client");
    }

    @Test
    public void testFlow() throws Exception {
        JMeterUtils.setProperty("blazemeter.client", BLCEmul.class.getName());
        BlazeMeterUploader uploader = new BlazeMeterUploader();
        uploader.setGui(new BlazeMeterUploaderGui());
        uploader.setShareTest(true);
        uploader.setProject("project");
        uploader.setTitle("title");
        uploader.testStarted();
        uploader.testEnded();

        assertEquals(true, uploader.isShareTest());
        assertEquals("project", uploader.getProject());
        assertEquals("title", uploader.getTitle());
        assertEquals("", uploader.getUploadToken());
    }

    @Test
    public void testClone() throws Exception {
        BlazeMeterUploader uploader = new BlazeMeterUploader();
        BlazeMeterUploaderGui gui = new BlazeMeterUploaderGui();
        uploader.setGui(gui);
        BlazeMeterUploader clone = (BlazeMeterUploader) uploader.clone();
        assertEquals(gui, clone.gui);
    }

}