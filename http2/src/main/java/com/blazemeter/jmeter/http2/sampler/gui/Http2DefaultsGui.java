/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.blazemeter.jmeter.http2.sampler.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

import com.blazemeter.jmeter.http2.sampler.HTTP2Request;

/**
 * GUI for Http Request defaults
 */
public class Http2DefaultsGui extends AbstractConfigGui {

    private static final long serialVersionUID = 241L;

    private HTTP2RequestPanel http2RequestPanel;
    private JCheckBox retrieveEmbeddedResources;
    private JCheckBox useMD5;
    private JLabeledTextField embeddedResourceUrlRegexFilter;
    private JTextField sourceIpAddr;
    private JComboBox<String> sourceIpType = new JComboBox<>(HTTPSamplerBase.getSourceTypeList());

    public Http2DefaultsGui() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        http2RequestPanel = new HTTP2RequestPanel(false);

        JPanel advancedPanel = new VerticalPanel();
        advancedPanel.add(createEmbeddedRsrcPanel());
        advancedPanel.add(createSourceAddrPanel());
        advancedPanel.add(createOptionalTasksPanel());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add(JMeterUtils
                .getResString("web_testing_basic"), http2RequestPanel);
        tabbedPane.add(JMeterUtils
                .getResString("web_testing_advanced"), advancedPanel);

        JPanel logoPanel = new JPanel();
        logoPanel.add(new BlazemeterLabsLogo());

        add(makeTitlePanel(), BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(logoPanel, BorderLayout.SOUTH);
    }

    private JPanel createEmbeddedRsrcPanel() {
        // retrieve Embedded resources
        retrieveEmbeddedResources = new JCheckBox(JMeterUtils.getResString("web_testing_retrieve_images")); // $NON-NLS-1$

        final JPanel embeddedRsrcPanel = new HorizontalPanel();
        embeddedRsrcPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("web_testing_retrieve_title"))); // $NON-NLS-1$
        embeddedRsrcPanel.add(retrieveEmbeddedResources);

        embeddedResourceUrlRegexFilter = new JLabeledTextField(JMeterUtils.getResString("web_testing_embedded_url_pattern"), 20); // $NON-NLS-1$
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
        // OPTIONAL TASKS
        final JPanel checkBoxPanel = new VerticalPanel();
        checkBoxPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("optional_tasks"))); // $NON-NLS-1$
        useMD5 = new JCheckBox(JMeterUtils.getResString("response_save_as_md5")); // $NON-NLS-1$
        checkBoxPanel.add(useMD5);
        return checkBoxPanel;
    }

    @Override
    public String getLabelResource() {
        return "HTTP2 Request Defaults";
    }

    @Override
    public String getStaticLabel() {
        return "HTTP2 Request Defaults";
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        ConfigTestElement config = new ConfigTestElement();
        modifyTestElement(config);
        return config;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement config) {
        ConfigTestElement cfg = (ConfigTestElement) config;
        ConfigTestElement el = (ConfigTestElement) http2RequestPanel.createTestElement();
        cfg.clear();
        cfg.addConfigElement(el);
        super.configureTestElement(config);
        if (retrieveEmbeddedResources.isSelected()) {
            config.setProperty(new BooleanProperty(HTTP2Request.EMBEDDED_RESOURCES, true));
        } else {
            config.removeProperty(HTTP2Request.EMBEDDED_RESOURCES);
        }
        if (useMD5.isSelected()) {
            config.setProperty(new BooleanProperty(HTTP2Request.MD5, true));
        } else {
            config.removeProperty(HTTP2Request.MD5);
        }
        if (!StringUtils.isEmpty(embeddedResourceUrlRegexFilter.getText())) {
            config.setProperty(new StringProperty(HTTP2Request.EMBEDDED_URL_REGEX,
                    embeddedResourceUrlRegexFilter.getText()));
        } else {
            config.removeProperty(HTTP2Request.EMBEDDED_URL_REGEX);
        }

        if (!StringUtils.isEmpty(sourceIpAddr.getText())) {
            config.setProperty(new StringProperty(HTTP2Request.IP_SOURCE,
                    sourceIpAddr.getText()));
            config.setProperty(new IntegerProperty(HTTP2Request.IP_SOURCE_TYPE,
                    sourceIpType.getSelectedIndex()));
        } else {
            config.removeProperty(HTTP2Request.IP_SOURCE);
            config.removeProperty(HTTP2Request.IP_SOURCE_TYPE);
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();
        retrieveEmbeddedResources.setSelected(false);
        useMD5.setSelected(false);
        http2RequestPanel.clear();
        embeddedResourceUrlRegexFilter.setText(""); // $NON-NLS-1$
        sourceIpAddr.setText(""); // $NON-NLS-1$
        sourceIpType.setSelectedIndex(HTTPSamplerBase.SourceType.HOSTNAME.ordinal()); //default: IP/Hostname
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        AbstractTestElement http2Sampler = (AbstractTestElement) el;
        http2RequestPanel.configure(el);
        retrieveEmbeddedResources.setSelected(http2Sampler.getPropertyAsBoolean(HTTP2Request.EMBEDDED_RESOURCES));
        useMD5.setSelected(http2Sampler.getPropertyAsBoolean(HTTP2Request.MD5, false));
        embeddedResourceUrlRegexFilter.setText(http2Sampler.getPropertyAsString(HTTP2Request.EMBEDDED_URL_REGEX, ""));//$NON-NLS-1$
        sourceIpAddr.setText(http2Sampler.getPropertyAsString(HTTP2Request.IP_SOURCE)); //$NON-NLS-1$
        sourceIpType.setSelectedIndex(
                http2Sampler.getPropertyAsInt(HTTP2Request.IP_SOURCE_TYPE,
                        HTTP2Request.SOURCE_TYPE_DEFAULT));
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

}
