package com.blazemeter.jmeter.http2.sampler.gui;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.protocol.http.gui.HTTPFileArgsPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;

import com.blazemeter.jmeter.http2.sampler.HTTP2Request;

import javax.swing.*;

public class HTTP2RequestPanel extends JPanel {

    private static final int TAB_PARAMETERS = 0;

    private HTTPArgumentsPanel argsPanel;

    // handle tabs
    private int tabRawBodyIndex = 1;
    private int tabFileUploadIndex = 2;

    private HTTPFileArgsPanel filesPanel;

    private JPanel webRequestPanel = new JPanel();
    private JPanel webServerPanel = new JPanel();
    private JPanel timeoutPanel = new JPanel();

    private JLabel requestIdLabel = new JLabel();
    private JLabel contentEncodingLabel = new JLabel();
    private JLabel contextPathLabel = new JLabel();

    private JLabel protocolLabel = new JLabel();
    private JLabel responseTimeoutLabel = new JLabel();
    private JLabel domainLabel = new JLabel();
    private JLabel portLabel = new JLabel();

    private JTextField requestId = new JTextField();
    private JTextField contentEncoding = new JTextField();
    private JTextField path = new JTextField();

    private JTextField protocol = new JTextField();
    private JPanel parametersPanel = new JPanel();
    private JTextField responseTimeout = new JTextField();
    private JTextField domain = new JTextField(20);
    private JTextField port = new JTextField();

    private JCheckBox autoRedirects = new JCheckBox();
    private JCheckBox followRedirects = new JCheckBox();
    private JCheckBox syncRequest = new JCheckBox();

    private JLabel methodLabel = new JLabel();
    private JComboBox<String> method = new JComboBox<>();

    private JLabel http2ImplementationLabel = new JLabel();
    private JComboBox<String> http2Implementation = new JComboBox<>();

    // Tabbed pane that contains parameters and raw body
    private ValidationTabbedPane postContentTabbedPane;

    private final boolean notConfigOnly;

    private JSyntaxTextArea postBodyContent;

    private boolean showFileUploadPane;

    public HTTP2RequestPanel(boolean showSamplerFields) {
        this(showSamplerFields, false);
    }

    /**
     * @param showSamplerFields  flag whether sampler fields should be shown
     * @param showFileUploadPane flag whether the file upload pane should be shown
     */
    private HTTP2RequestPanel(boolean showSamplerFields, boolean showFileUploadPane) {
        this.notConfigOnly = showSamplerFields;
        this.showFileUploadPane = showFileUploadPane;
        init();
    }

    private void init() {
        initComponents();

        postContentTabbedPane = new ValidationTabbedPane();
        argsPanel = new HTTPArgumentsPanel();
        postContentTabbedPane.add(JMeterUtils.getResString("post_as_parameters"), argsPanel);// $NON-NLS-1$

        int indx = TAB_PARAMETERS;

        tabRawBodyIndex = ++indx;
        postBodyContent = JSyntaxTextArea.getInstance(30, 50);// $NON-NLS-1$
        postContentTabbedPane.add(JMeterUtils.getResString("post_body"), JTextScrollPane.getInstance(postBodyContent));// $NON-NLS-1$

        tabFileUploadIndex = ++indx;
        filesPanel = new HTTPFileArgsPanel();
        postContentTabbedPane.add(JMeterUtils.getResString("post_files_upload"), filesPanel);


        parametersPanel.add(postContentTabbedPane);
    }

    private void initComponents() {
        domainLabel.setText(JMeterUtils.getResString("web_server_domain"));
        portLabel.setText("Port Number:");

        webServerPanel.setBorder(BorderFactory.createTitledBorder("Web Server"));

        GroupLayout webServerPanelLayout = new javax.swing.GroupLayout(webServerPanel);
        webServerPanel.setLayout(webServerPanelLayout);
        webServerPanelLayout.setHorizontalGroup(
                webServerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(webServerPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(domainLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(domain)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(portLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );
        webServerPanelLayout.setVerticalGroup(
                webServerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(webServerPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(webServerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(domainLabel)
                                        .addComponent(domain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(portLabel)
                                        .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        timeoutPanel.setBorder(BorderFactory.createTitledBorder("Timeout (miliseconds)"));
        responseTimeoutLabel.setText("Response:");


        javax.swing.GroupLayout timeoutPanelLayout = new javax.swing.GroupLayout(timeoutPanel);
        timeoutPanel.setLayout(timeoutPanelLayout);
        timeoutPanelLayout.setHorizontalGroup(
                timeoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(timeoutPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(responseTimeoutLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(responseTimeout)
                                .addContainerGap())
        );
        timeoutPanelLayout.setVerticalGroup(
                timeoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(timeoutPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(timeoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(responseTimeoutLabel)
                                        .addComponent(responseTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        requestIdLabel.setText("Request Id:");

        contentEncodingLabel.setText("Content encoding:");
        contextPathLabel.setText("Path:");

        protocolLabel.setText("Protocol [http/https]:");

        methodLabel.setText("Method:");
        String[] methodList = HTTPSamplerBase.getValidMethodsAsArray();
        for (String aMethodList : methodList) method.addItem(aMethodList);
        http2ImplementationLabel.setText(JMeterUtils.getResString("http_implementation"));
        http2Implementation.addItem("Jetty");

        webRequestPanel.setBorder(BorderFactory.createTitledBorder("HTTP Request"));


        protocol.setToolTipText("");

        parametersPanel.setLayout(new BoxLayout(parametersPanel, BoxLayout.LINE_AXIS));

        autoRedirects.setText("Redirect Automatically");
        followRedirects.setText("Follow Redirects");
        syncRequest.setText("Synchronized Request");

        javax.swing.GroupLayout webRequestPanelLayout = new javax.swing.GroupLayout(webRequestPanel);
        webRequestPanel.setLayout(webRequestPanelLayout);

        if (notConfigOnly) {
            webRequestPanelLayout.setHorizontalGroup(
                    webRequestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(webRequestPanelLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(webRequestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(parametersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(webRequestPanelLayout.createSequentialGroup()
                                                    .addComponent(http2ImplementationLabel)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(http2Implementation)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(protocolLabel)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(protocol, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(methodLabel)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(method, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(contentEncodingLabel)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(contentEncoding, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(webRequestPanelLayout.createSequentialGroup()
                                                    .addGroup(webRequestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addGroup(webRequestPanelLayout.createSequentialGroup()
                                                                    .addComponent(autoRedirects)
                                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                    .addComponent(followRedirects)
                                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                    .addComponent(syncRequest)
                                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                                                    .addGap(0, 0, Short.MAX_VALUE))
                                            .addGroup(webRequestPanelLayout.createSequentialGroup()
                                                    .addComponent(contextPathLabel)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(path)))
                                    .addContainerGap())
            );
            webRequestPanelLayout.setVerticalGroup(
                    webRequestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(webRequestPanelLayout.createSequentialGroup()
                                    .addGap(10, 10, 10)
                                    .addGroup(webRequestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(http2ImplementationLabel)
                                            .addComponent(http2Implementation)
                                            .addComponent(protocolLabel)
                                            .addComponent(protocol)
                                            .addComponent(methodLabel)
                                            .addComponent(method)
                                            .addComponent(contentEncodingLabel)
                                            .addComponent(contentEncoding))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(webRequestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(contextPathLabel)
                                            .addComponent(path, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(webRequestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(autoRedirects)
                                            .addComponent(followRedirects)
                                            .addComponent(syncRequest))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(parametersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                                    .addContainerGap())
            );
        } else {
            webRequestPanelLayout.setHorizontalGroup(
                    webRequestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(webRequestPanelLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(webRequestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(parametersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(webRequestPanelLayout.createSequentialGroup()
                                                    .addComponent(http2ImplementationLabel)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(http2Implementation)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(protocolLabel)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(protocol, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(contentEncodingLabel)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(contentEncoding, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                                            .addGroup(webRequestPanelLayout.createSequentialGroup()
                                                    .addGroup(webRequestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addGroup(webRequestPanelLayout.createSequentialGroup()
                                                                    .addComponent(syncRequest)
                                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                                                    .addGap(0, 0, Short.MAX_VALUE))
                                            .addGroup(webRequestPanelLayout.createSequentialGroup()
                                                    .addComponent(contextPathLabel)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(path)))
                                    .addContainerGap())
            );
            webRequestPanelLayout.setVerticalGroup(
                    webRequestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(webRequestPanelLayout.createSequentialGroup()
                                    .addGap(10, 10, 10)
                                    .addGroup(webRequestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(http2ImplementationLabel)
                                            .addComponent(http2Implementation)
                                            .addComponent(protocolLabel)
                                            .addComponent(protocol)
                                            .addComponent(contentEncodingLabel)
                                            .addComponent(contentEncoding))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(webRequestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(contextPathLabel)
                                            .addComponent(path, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(webRequestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(syncRequest))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(parametersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                                    .addContainerGap())
            );
        }


        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(webRequestPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(webServerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(timeoutPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                )
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(timeoutPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(webServerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(webRequestPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
        );
    }

    class ValidationTabbedPane extends JTabbedPane {

        private static final long serialVersionUID = 7014311238367882880L;

        @Override
        public void setSelectedIndex(int index) {
            setSelectedIndex(index, true);
        }

        /**
         * Apply some check rules if check is true
         *
         * @param index index to select
         * @param check flag whether to perform checks before setting the selected
         *              index
         */
        private void setSelectedIndex(int index, boolean check) {
            int oldSelectedIndex = this.getSelectedIndex();
            if (!check || oldSelectedIndex == -1) {
                super.setSelectedIndex(index);
            } else if (index != oldSelectedIndex) {
                // If there is no data, then switching between Parameters/file upload and Raw should be
                // allowed with no further user interaction.
                if (noData(oldSelectedIndex)) {
                    argsPanel.clear();
                    postBodyContent.setInitialText("");
                    filesPanel.clear();

                    super.setSelectedIndex(index);
                } else {
                    boolean filePanelHasData;
                    filePanelHasData = filesPanel.hasData();

                    if (oldSelectedIndex == tabRawBodyIndex) {

                        // If RAW data and Parameters match we allow switching
                        if (index == TAB_PARAMETERS && postBodyContent.getText().equals(computePostBody((Arguments) argsPanel.createTestElement()).trim())) {
                            super.setSelectedIndex(index);
                        } else {
                            // If there is data in the Raw panel, then the user should be
                            // prevented from switching (that would be easy to track).
                            JOptionPane.showConfirmDialog(this,
                                    JMeterUtils.getResString("web_cannot_switch_tab"), // $NON-NLS-1$
                                    JMeterUtils.getResString("warning"), // $NON-NLS-1$
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        // can switch from parameter to fileupload
                        if ((oldSelectedIndex == TAB_PARAMETERS
                                && index == tabFileUploadIndex)
                                || (oldSelectedIndex == tabFileUploadIndex
                                && index == TAB_PARAMETERS)) {
                            super.setSelectedIndex(index);
                            return;
                        }

                        // If the Parameter data can be converted (i.e. no names) and there is no data in file upload
                        // we warn the user that the Parameter data will be lost.
                        if (oldSelectedIndex == TAB_PARAMETERS && !filePanelHasData && canConvertParameters()) {
                            Object[] options = {
                                    JMeterUtils.getResString("confirm"), // $NON-NLS-1$
                                    JMeterUtils.getResString("cancel")}; // $NON-NLS-1$
                            int n = JOptionPane.showOptionDialog(this,
                                    JMeterUtils.getResString("web_parameters_lost_message"), // $NON-NLS-1$
                                    JMeterUtils.getResString("warning"), // $NON-NLS-1$
                                    JOptionPane.YES_NO_CANCEL_OPTION,
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    options,
                                    options[1]);
                            if (n == JOptionPane.YES_OPTION) {
                                convertParametersToRawBody();
                                super.setSelectedIndex(index);
                            }
                        } else {
                            // If the Parameter data cannot be converted to Raw, then the user should be
                            // prevented from doing so raise an error dialog
                            String messageKey = filePanelHasData ? "web_cannot_switch_tab" : "web_cannot_convert_parameters_to_raw";
                            JOptionPane.showConfirmDialog(this,
                                    JMeterUtils.getResString(messageKey), // $NON-NLS-1$
                                    JMeterUtils.getResString("warning"), // $NON-NLS-1$
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if no data is available in the selected tab
     *
     * @param oldSelectedIndex the tab to check for data
     * @return true if neither Parameters tab nor Raw Body tab contain data
     */
    private boolean noData(int oldSelectedIndex) {
        if (oldSelectedIndex == tabRawBodyIndex) {
            return StringUtils.isEmpty(postBodyContent.getText().trim());
        } else {
            Arguments element = (Arguments) argsPanel.createTestElement();
            return !filesPanel.hasData() && StringUtils.isEmpty(computePostBody(element));
        }
    }

    /**
     * Compute body data from arguments
     *
     * @param arguments {@link Arguments}
     * @return {@link String}
     */
    private static String computePostBody(Arguments arguments) {
        return computePostBody(arguments, false);
    }

    /**
     * @return true if no argument has a name
     */
    private boolean canConvertParameters() {
        Arguments arguments = (Arguments) argsPanel.createTestElement();
        for (int i = 0; i < arguments.getArgumentCount(); i++) {
            if (!StringUtils.isEmpty(arguments.getArgument(i).getName())) {
                return false;
            }
        }
        return true;
    }

    private void convertParametersToRawBody() {
        postBodyContent.setInitialText(computePostBody((Arguments) argsPanel.createTestElement()));
        postBodyContent.setCaretPosition(0);
    }

    public TestElement createTestElement() {
        ConfigTestElement element = new ConfigTestElement();
        element.setName(this.getName());
        element.setProperty(TestElement.GUI_CLASS, this.getClass().getName());
        element.setProperty(TestElement.TEST_CLASS, element.getClass().getName());
        modifyTestElement(element);
        return element;
    }

    /**
     * Save the GUI values in the sampler.
     *
     * @param element {@link TestElement} to modify
     */
    public void modifyTestElement(TestElement element) {
        boolean useRaw = postContentTabbedPane.getSelectedIndex() == tabRawBodyIndex;
        Arguments args;
        if (useRaw) {
            args = new Arguments();
            String text = postBodyContent.getText();
            /*
             * Textfield uses \n (LF) to delimit lines; we need to send CRLF.
             * Rather than change the way that arguments are processed by the
             * samplers for raw data, it is easier to fix the data.
             * On retrieval, CRLF is converted back to LF for storage in the text field.
             * See
             */
            HTTPArgument arg = new HTTPArgument("", text.replaceAll("\n", "\r\n"), false);
            arg.setAlwaysEncoded(false);
            args.addArgument(arg);
        } else {
            args = (Arguments) argsPanel.createTestElement();
            HTTPArgument.convertArgumentsToHTTP(args);
            if (showFileUploadPane) {
                filesPanel.modifyTestElement(element);
            }
        }
        element.setProperty(HTTP2Request.POST_BODY_RAW, useRaw, HTTP2Request.POST_BODY_RAW_DEFAULT);
        element.setProperty(new TestElementProperty(HTTP2Request.ARGUMENTS, args));
        element.setProperty(HTTP2Request.DOMAIN, domain.getText());
        element.setProperty(HTTP2Request.PORT, port.getText());
        element.setProperty(HTTP2Request.RESPONSE_TIMEOUT, responseTimeout.getText());
        element.setProperty(HTTP2Request.PROTOCOL, protocol.getText());
        element.setProperty(HTTP2Request.CONTENT_ENCODING, contentEncoding.getText());
        element.setProperty(HTTP2Request.PATH, path.getText());
        if (notConfigOnly) {
            element.setProperty(HTTP2Request.METHOD, (String) method.getSelectedItem());
            element.setProperty(new BooleanProperty(HTTP2Request.FOLLOW_REDIRECTS, followRedirects.isSelected()));
            element.setProperty(new BooleanProperty(HTTP2Request.AUTO_REDIRECTS, autoRedirects.isSelected()));
            element.setProperty(HTTP2Request.REQUEST_ID, requestId.getText());
        }
        if (syncRequest.isSelected()) {
            element.setProperty(new BooleanProperty(HTTP2Request.SYNC_REQUEST, true));
        } else {
            element.removeProperty(HTTP2Request.SYNC_REQUEST);
        }
    }

    public void configure(TestElement el) {
        setName(el.getName());
        Arguments arguments = (Arguments) el.getProperty(HTTP2Request.ARGUMENTS).getObjectValue();

        boolean useRaw = el.getPropertyAsBoolean(HTTP2Request.POST_BODY_RAW, HTTP2Request.POST_BODY_RAW_DEFAULT);
        if (useRaw) {
            String postBody = computePostBody(arguments, true); // Convert CRLF to CR, see modifyTestElement
            postBodyContent.setInitialText(postBody);
            postBodyContent.setCaretPosition(0);
            postContentTabbedPane.setSelectedIndex(tabRawBodyIndex, false);
        } else {
            argsPanel.configure(arguments);
            postContentTabbedPane.setSelectedIndex(TAB_PARAMETERS, false);
            if (showFileUploadPane) {
                filesPanel.configure(el);
            }
        }

        domain.setText(el.getPropertyAsString(HTTP2Request.DOMAIN));

        String portString = el.getPropertyAsString(HTTP2Request.PORT);

        // Only display the port number if it is meaningfully specified
        if (portString.equals(HTTP2Request.UNSPECIFIED_PORT_AS_STRING)) {
            port.setText(""); // $NON-NLS-1$
        } else {
            port.setText(portString);
        }
        responseTimeout.setText(el.getPropertyAsString(HTTP2Request.RESPONSE_TIMEOUT));
        protocol.setText(el.getPropertyAsString(HTTP2Request.PROTOCOL));
        contentEncoding.setText(el.getPropertyAsString(HTTP2Request.CONTENT_ENCODING));
        path.setText(el.getPropertyAsString(HTTP2Request.PATH));
        if (notConfigOnly) {
            method.setSelectedItem(el.getPropertyAsString(HTTP2Request.METHOD));
            followRedirects.setSelected(el.getPropertyAsBoolean(HTTP2Request.FOLLOW_REDIRECTS));
            autoRedirects.setSelected(el.getPropertyAsBoolean(HTTP2Request.AUTO_REDIRECTS));
            requestId.setText(el.getPropertyAsString(HTTP2Request.REQUEST_ID));
        }
        syncRequest.setSelected(el.getPropertyAsBoolean(HTTP2Request.SYNC_REQUEST));
    }

    /**
     * Compute body data from arguments
     *
     * @param arguments {@link Arguments}
     * @param crlfToLF  whether to convert CRLF to LF
     * @return {@link String}
     */
    private static String computePostBody(Arguments arguments, boolean crlfToLF) {
        StringBuilder postBody = new StringBuilder();
        for (JMeterProperty argument : arguments) {
            HTTPArgument arg = (HTTPArgument) argument.getObjectValue();
            String value = arg.getValue();
            if (crlfToLF) {
                value = value.replaceAll("\r\n", "\n"); // See modifyTestElement
            }
            postBody.append(value);
        }
        return postBody.toString();
    }

    public void clear() {
        domain.setText("");
        port.setText(""); // $NON-NLS-1$
        responseTimeout.setText(""); // $NON-NLS-1$

        http2Implementation.setSelectedItem(""); // $NON-NLS-1$
        protocol.setText(""); // $NON-NLS-1$
        http2Implementation.setSelectedItem(HTTP2Request.DEFAULT_METHOD); // $NON-NLS-1$
        contentEncoding.setText(""); // $NON-NLS-1$
        requestId.setText("");

        path.setText(""); // $NON-NLS-1$

        syncRequest.setSelected(false);

        argsPanel.clear();
        postBodyContent.setInitialText("");
        filesPanel.clear();

        postContentTabbedPane.setSelectedIndex(TAB_PARAMETERS, false);
        if (notConfigOnly) {
            followRedirects.setSelected(true);
            autoRedirects.setSelected(false);
            method.setSelectedItem(HTTPSamplerBase.DEFAULT_METHOD);
        }

    }

}
