package kg.apc.jmeter.reporters.bzm;

import kg.apc.jmeter.JMeterPluginsUtils;
import kg.apc.jmeter.gui.GuiBuilderHelper;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

public class BlazeMeterUploaderGui extends AbstractListenerGui implements HyperlinkListener, Clearable, UnsharedComponent {

    public static final String WIKIPAGE = "BlazeMeterUploader";
    public static final String UPLOAD_TOKEN_PLACEHOLDER = "Replace this text with upload token received at a.blazemeter.com\nCan be used deprecated API keys or new improved keys.\nEmpty token means anonymous report without any other settings required.\nRemember that anyone who has this token can upload files to your account.\nPlease, treat your token as confidential data.\nSee plugin help for details.";

    private JCheckBox shareTest;
    private JTextField projectKey;
    private JTextField testTitle;
    private JTextArea uploadToken;
    private JTextPane infoArea;
    private String infoText = "";

    public BlazeMeterUploaderGui() {
        super();
        init();
        initFields();
    }

    @Override
    public String getStaticLabel() {
        return "bzm - BlazeMeter Uploader";
    }

    @Override
    public String getLabelResource() {
        return getClass().getCanonicalName();
    }

    @Override
    public TestElement createTestElement() {
        TestElement te = new BlazeMeterUploader();
        modifyTestElement(te);
        te.setComment(JMeterPluginsUtils.getWikiLinkText(WIKIPAGE));
        return te;
    }

    @Override
    public void modifyTestElement(TestElement te) {
        super.configureTestElement(te);
        if (te instanceof BlazeMeterUploader) {
            BlazeMeterUploader uploader = (BlazeMeterUploader) te;
            uploader.setShareTest(shareTest.isSelected());
            uploader.setTitle(testTitle.getText());
            uploader.setProject(projectKey.getText());
            uploader.setUploadToken(uploadToken.getText());
            uploader.setGui(this);
        }
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        BlazeMeterUploader uploader = (BlazeMeterUploader) element;
        shareTest.setSelected(uploader.isShareTest());
        projectKey.setText(uploader.getProject());
        testTitle.setText(uploader.getTitle());
        uploadToken.setText(uploader.getUploadToken());
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(JMeterPluginsUtils.addHelpLinkToPanel(makeTitlePanel(), WIKIPAGE), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.anchor = GridBagConstraints.FIRST_LINE_END;

        GridBagConstraints editConstraints = new GridBagConstraints();
        editConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        editConstraints.weightx = 1.0;
        editConstraints.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addToPanel(mainPanel, labelConstraints, 0, row, new JLabel("Share test: ", JLabel.RIGHT));
        addToPanel(mainPanel, editConstraints, 1, row, shareTest = new JCheckBox());

        row++;
        addToPanel(mainPanel, labelConstraints, 0, row, new JLabel("Upload to Project: ", JLabel.RIGHT));
        addToPanel(mainPanel, editConstraints, 1, row, projectKey = new JTextField(20));

        editConstraints.insets = new Insets(2, 0, 0, 0);
        labelConstraints.insets = new Insets(2, 0, 0, 0);

        row++;
        addToPanel(mainPanel, labelConstraints, 0, row, new JLabel("Test Title: ", JLabel.RIGHT));
        addToPanel(mainPanel, editConstraints, 1, row, testTitle = new JTextField(20));

        row++;
        addToPanel(mainPanel, labelConstraints, 0, row, new JLabel("Upload Token: ", JLabel.RIGHT));

        uploadToken = new JTextArea();
        uploadToken.setLineWrap(true);
        addToPanel(mainPanel, editConstraints, 1, row, GuiBuilderHelper.getTextAreaScrollPaneContainer(uploadToken, 6));

        row++;
        addToPanel(mainPanel, labelConstraints, 0, row, new JLabel("Info Area: ", JLabel.RIGHT));
        infoArea = new JTextPane();
        infoArea.setEditable(false);
        infoArea.setOpaque(false);
        infoArea.setContentType("text/html");
        infoArea.addHyperlinkListener(this);

        JScrollPane ret = new JScrollPane();
        ret.setViewportView(infoArea);
        addToPanel(mainPanel, editConstraints, 1, row, ret);

        ret.setMinimumSize(new Dimension(0, 200));
        ret.setPreferredSize(new Dimension(0, 200));
        ret.setSize(new Dimension(0, 200));

        JPanel container = new JPanel(new BorderLayout());
        container.add(mainPanel, BorderLayout.NORTH);
        add(container, BorderLayout.CENTER);
    }

    private void initFields() {
        shareTest.setSelected(false);
        testTitle.setText("");
        projectKey.setText("Default project");
        uploadToken.setText(UPLOAD_TOKEN_PLACEHOLDER);
    }

    private void addToPanel(JPanel panel, GridBagConstraints constraints, int col, int row, JComponent component) {
        constraints.gridx = col;
        constraints.gridy = row;
        panel.add(component, constraints);
    }

    @Override
    public void clearGui() {
        super.clearGui();
        initFields();
    }

    @Override
    public void clearData() {
        infoText = "";
        infoArea.setText("");
    }

    public void inform(String string) {
        infoText += string + "<br/>\n";
        infoArea.setText(infoText);
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            JMeterPluginsUtils.openInBrowser(e.getURL().toString());
        }
    }
}
