package kg.apc.jmeter.reporters.bzm;

import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.testelement.TestElement;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.Assert.assertEquals;

public class BlazemeterUploaderGuiTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        TestJMeterUtils.createJmeterEnv();
    }

//    @Test
    public void displayGUI() throws InterruptedException {
        if (!GraphicsEnvironment.isHeadless()) {
            BlazeMeterUploaderGui obj = new BlazeMeterUploaderGui();
            TestElement te = obj.createTestElement();
            obj.configure(te);
            obj.clearGui();
            obj.modifyTestElement(te);

            JFrame frame = new JFrame(obj.getStaticLabel());
            frame.setPreferredSize(new Dimension(800, 600));
            frame.getContentPane().add(obj, BorderLayout.CENTER);
            frame.pack();
            frame.setVisible(true);

            while (frame.isVisible()) {
                Thread.sleep(1000);
            }
        }
    }

    @Test
    public void testGui() throws Exception {
        BlazeMeterUploaderGui gui = new BlazeMeterUploaderGui();

        assertEquals(BlazeMeterUploaderGui.class.getCanonicalName(), gui.getLabelResource());
        assertEquals("bzm - BlazeMeter Uploader", gui.getStaticLabel());

        BlazeMeterUploader element1 = (BlazeMeterUploader) gui.createTestElement();
        BlazeMeterUploader element2 = (BlazeMeterUploader) gui.createTestElement();

        element1.setProject("test_project");
        element1.setTitle("test_title");
        element1.setUploadToken("test_token");
        element1.setShareTest(true);

        gui.configure(element1);
        gui.modifyTestElement(element2);

        assertEquals(element1.getProject(), element2.getProject());
        assertEquals(element1.getTitle(), element2.getTitle());
        assertEquals(element1.getUploadToken(), element2.getUploadToken());
        assertEquals(element1.isShareTest(), element2.isShareTest());

        gui.clearGui();
        gui.modifyTestElement(element2);

        assertEquals("", element2.getTitle());
        assertEquals("Default project", element2.getProject());
        assertEquals(BlazeMeterUploaderGui.UPLOAD_TOKEN_PLACEHOLDER, element2.getUploadToken());
        assertEquals(false, element2.isShareTest());
    }
}