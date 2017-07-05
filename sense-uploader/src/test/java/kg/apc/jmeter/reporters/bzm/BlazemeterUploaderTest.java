package kg.apc.jmeter.reporters.bzm;

import kg.apc.emulators.TestJMeterUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BlazemeterUploaderTest {

    @BeforeClass
    public static void setUpClass()
            throws Exception {
        TestJMeterUtils.createJmeterEnv();
    }

    @Test
    public void testFlow() throws Exception {
        BlazemeterUploader uploader = new BlazemeterUploader();
        uploader.setGui(new BlazemeterUploaderGui());
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
        BlazemeterUploader uploader = new BlazemeterUploader();
        BlazemeterUploaderGui gui = new BlazemeterUploaderGui();
        uploader.setGui(gui);
        BlazemeterUploader clone = (BlazemeterUploader) uploader.clone();
        assertEquals(gui, clone.gui);
    }
}