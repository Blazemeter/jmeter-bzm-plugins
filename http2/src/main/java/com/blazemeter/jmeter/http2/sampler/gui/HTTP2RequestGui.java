package com.blazemeter.jmeter.http2.sampler.gui;

import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.blazemeter.jmeter.http2.sampler.HTTP2Request;

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

public class HTTP2RequestGui extends AbstractSamplerGui {

    private static final Logger log = LoggingManager.getLoggerForClass();
    
    private JCheckBox retrieveEmbeddedResources;
      
    private HTTP2RequestPanel http2RequestPanel;
    
    private JCheckBox useMD5;
    
    private JLabeledTextField embeddedRE; // regular expression used to match against embedded resource URLs

    private JTextField sourceIpAddr; // does not apply to Java implementation
    
    private JComboBox<String> sourceIpType = new JComboBox<>(HTTPSamplerBase.getSourceTypeList());

    public HTTP2RequestGui(){
        super();
        init();

        
    }
    
    protected JPanel createEmbeddedRsrcPanel() {
        // retrieve Embedded resources
        retrieveEmbeddedResources = new JCheckBox(JMeterUtils.getResString("web_testing_retrieve_images")); // $NON-NLS-1$

        final JPanel embeddedRsrcPanel = new HorizontalPanel();
        embeddedRsrcPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("web_testing_retrieve_title"))); // $NON-NLS-1$
        embeddedRsrcPanel.add(retrieveEmbeddedResources);

        
        // Embedded URL match regex
        embeddedRE = new JLabeledTextField(JMeterUtils.getResString("web_testing_embedded_url_pattern"),20); // $NON-NLS-1$
        embeddedRsrcPanel.add(embeddedRE); 
        
        return embeddedRsrcPanel;
    }
    
    protected JPanel createSourceAddrPanel() {
        final JPanel sourceAddrPanel = new HorizontalPanel();
        sourceAddrPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("web_testing_source_ip"))); // $NON-NLS-1$

        sourceIpType.setSelectedIndex(HTTPSamplerBase.SourceType.HOSTNAME.ordinal()); //default: IP/Hostname
        sourceAddrPanel.add(sourceIpType);

        sourceIpAddr = new JTextField();
        sourceAddrPanel.add(sourceIpAddr);
        return sourceAddrPanel;
    }
    
    protected JPanel createOptionalTasksPanel() {
        // OPTIONAL TASKS
        final JPanel checkBoxPanel = new VerticalPanel();
        checkBoxPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("optional_tasks"))); // $NON-NLS-1$

        // Use MD5
        useMD5 = new JCheckBox(JMeterUtils.getResString("response_save_as_md5")); // $NON-NLS-1$

        checkBoxPanel.add(useMD5);

        return checkBoxPanel;
    }

    @Override
    public String getStaticLabel() {
        return "HTTP2 Request";
    }

    public String getLabelResource() {
        return "HTTP2 Request";
    }

    public TestElement createTestElement() {
        HTTP2Request sampler = new HTTP2Request();
        modifyTestElement(sampler);
        return sampler;
    }
    
    
    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        final HTTP2Request http2sampler = (HTTP2Request) element;
        http2RequestPanel.configure(element);
        retrieveEmbeddedResources.setSelected(http2sampler.isEmbeddedResources());
        //useMD5.setSelected(samplerBase.useMD5());
        embeddedRE.setText(http2sampler.getEmbeddedUrlRE());
        /*if (!isAJP) {
            sourceIpAddr.setText(samplerBase.getIpSource());
            sourceIpType.setSelectedIndex(samplerBase.getIpSourceType());
        }*/
    }
    
    @Override
    public void modifyTestElement(TestElement sampler) {
        sampler.clear();
        http2RequestPanel.modifyTestElement(sampler);
        final HTTP2Request http2Sample = (HTTP2Request) sampler;
        //TODO
        http2Sample.setEmbeddedResources(retrieveEmbeddedResources.isSelected());
        //samplerBase.setMD5(useMD5.isSelected());
        http2Sample.setEmbeddedUrlRE(embeddedRE.getText());
        /*if (!isAJP) {
            samplerBase.setIpSource(sourceIpAddr.getText());
            samplerBase.setIpSourceType(sourceIpType.getSelectedIndex());
        }*/
        super.configureTestElement(sampler);
    }


    
    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }
    

    @Override
    public void clearGui() {
        super.clearGui();
        retrieveEmbeddedResources.setSelected(false);
        useMD5.setSelected(false);
        embeddedRE.setText(""); // $NON-NLS-1$
        sourceIpAddr.setText(""); // $NON-NLS-1$
        sourceIpType.setSelectedIndex(HTTPSamplerBase.SourceType.HOSTNAME.ordinal()); //default: IP/Hostname
        http2RequestPanel.clear();
    }

    private void init() {
    	setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        
        http2RequestPanel = new HTTP2RequestPanel(true,true,true);
        
        // AdvancedPanel (embedded resources, source address and optional tasks)
        JPanel advancedPanel = new VerticalPanel();
        advancedPanel.add(createEmbeddedRsrcPanel());
        advancedPanel.add(createSourceAddrPanel());
        advancedPanel.add(createOptionalTasksPanel());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add(JMeterUtils
                .getResString("web_testing_basic"), http2RequestPanel);
        tabbedPane.add(JMeterUtils
                .getResString("web_testing_advanced"), advancedPanel);

        JPanel emptyPanel = new JPanel();
        emptyPanel.setMaximumSize(new Dimension());

        add(makeTitlePanel(), BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);        
        add(emptyPanel, BorderLayout.SOUTH);
    }
}
