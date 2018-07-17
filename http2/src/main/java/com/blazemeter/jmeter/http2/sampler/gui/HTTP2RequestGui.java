package com.blazemeter.jmeter.http2.sampler.gui;

import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

import com.blazemeter.jmeter.http2.sampler.HTTP2Request;

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

public class HTTP2RequestGui extends AbstractSamplerGui {

    private JCheckBox retrieveEmbeddedResources;
    private HTTP2RequestPanel http2RequestPanel;
    private JCheckBox useMD5;
    private JLabeledTextField embeddedResourceUrlRegexFilter;
    private JTextField sourceIpAddr;
    private JComboBox<String> sourceIpType = new JComboBox<>(HTTPSamplerBase.getSourceTypeList());

    public HTTP2RequestGui(){
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        JPanel logoPanel = new JPanel();
        logoPanel.add(new BlazemeterLabsLogo());

        http2RequestPanel = new HTTP2RequestPanel(true);

        JPanel advancedPanel = new VerticalPanel();
        advancedPanel.add(createEmbeddedRsrcPanel());
        advancedPanel.add(createSourceAddrPanel());
        advancedPanel.add(createOptionalTasksPanel());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add(JMeterUtils
                .getResString("web_testing_basic"), http2RequestPanel);
        tabbedPane.add(JMeterUtils
                .getResString("web_testing_advanced"), advancedPanel);

        add(makeTitlePanel(), BorderLayout.NORTH);
        add(logoPanel, BorderLayout.SOUTH);
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createEmbeddedRsrcPanel() {
        retrieveEmbeddedResources = new JCheckBox(JMeterUtils.getResString("web_testing_retrieve_images")); // $NON-NLS-1$

        final JPanel embeddedRsrcPanel = new HorizontalPanel();
        embeddedRsrcPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("web_testing_retrieve_title"))); // $NON-NLS-1$
        embeddedRsrcPanel.add(retrieveEmbeddedResources);
        
        embeddedResourceUrlRegexFilter = new JLabeledTextField(JMeterUtils.getResString("web_testing_embedded_url_pattern"),20); // $NON-NLS-1$
        embeddedRsrcPanel.add(embeddedResourceUrlRegexFilter);
        
        return embeddedRsrcPanel;
    }
    
    private JPanel createSourceAddrPanel() {
        final JPanel sourceAddrPanel = new HorizontalPanel();
        sourceAddrPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("web_testing_source_ip"))); // $NON-NLS-1$

        sourceIpType.setSelectedIndex(HTTPSamplerBase.SourceType.HOSTNAME.ordinal()); //default: IP/Hostname
        sourceAddrPanel.add(sourceIpType);

        sourceIpAddr = new JTextField();
        sourceAddrPanel.add(sourceIpAddr);
        return sourceAddrPanel;
    }
    
    private JPanel createOptionalTasksPanel() {
        final JPanel checkBoxPanel = new VerticalPanel();
        checkBoxPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("optional_tasks"))); // $NON-NLS-1$
        useMD5 = new JCheckBox(JMeterUtils.getResString("response_save_as_md5")); // $NON-NLS-1$
        checkBoxPanel.add(useMD5);
        return checkBoxPanel;
    }

    @Override
    public String getStaticLabel() {
        return "HTTP2 Request";
    }

    @Override
    public String getLabelResource() {
        return "HTTP2 Request";
    }

    @Override
    public TestElement createTestElement() {
        HTTP2Request sampler = new HTTP2Request();
        modifyTestElement(sampler);
        return sampler;
    }
    
    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        final HTTP2Request http2sampler = (HTTP2Request) element;
        http2RequestPanel.configure(element);
        retrieveEmbeddedResources.setSelected(http2sampler.isEmbeddedResources());
        embeddedResourceUrlRegexFilter.setText(http2sampler.getEmbeddedUrlRE());
    }
    
    @Override
    public void modifyTestElement(TestElement sampler) {
        sampler.clear();
        http2RequestPanel.modifyTestElement(sampler);
        final HTTP2Request http2Sample = (HTTP2Request) sampler;
        //TODO
        http2Sample.setEmbeddedResources(retrieveEmbeddedResources.isSelected());
        http2Sample.setEmbeddedUrlRE(embeddedResourceUrlRegexFilter.getText());
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
        embeddedResourceUrlRegexFilter.setText(""); // $NON-NLS-1$
        sourceIpAddr.setText(""); // $NON-NLS-1$
        sourceIpType.setSelectedIndex(HTTPSamplerBase.SourceType.HOSTNAME.ordinal()); //default: IP/Hostname
        http2RequestPanel.clear();
    }

}
