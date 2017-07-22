
package blazemeter.jmeter.plugins.websocket.sampler.gui;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;


public class WebSocketConnectionSamplerPanel extends javax.swing.JPanel {
    

    private JPanel webServerPanel = new JPanel();
	private JLabel serverLabel = new JLabel();
	private JTextField server = new JTextField();
	private JLabel portLabel = new JLabel();
	private JTextField port = new JTextField();
	
	private JPanel timeoutPanel = new JPanel();
	private JLabel connectTimeoutLabel = new JLabel();
	private JTextField connectionTimeout = new JTextField();
	
	private JPanel requestPanel = new JPanel();
	private JLabel protocolLabel = new JLabel();
	private JTextField protocol = new JTextField();
	private JLabel pathLabel = new JLabel();
	private JTextField path = new JTextField();
	private JLabel contentEncodingLabel = new JLabel();
	private JTextField contentEncoding = new JTextField();
	private JLabel connectionIdLabel = new JLabel();
	private JTextField connectionId = new JTextField();
	private JLabel implementationLabel = new JLabel();
	private JComboBox implementationComboBox = new JComboBox();	
	private HTTPArgumentsPanel attributePanel;
    private JPanel querystringAttributesPanel = new JPanel();
	
	private JPanel responsePanel = new JPanel();
	private JPanel querystringPatternsPanel = new JPanel();
	private ArgumentsPanel patternsPanel;
	private JLabel closePatternLabel = new JLabel();
	private JTextField closePattern = new JTextField();
	
    
    public WebSocketConnectionSamplerPanel() {
        initComponents();
        attributePanel = new HTTPArgumentsPanel();
        patternsPanel = new ArgumentsPanel("Response Patterns");
        querystringAttributesPanel.add(attributePanel);
        querystringPatternsPanel.add(patternsPanel);
    }


    private void initComponents() {

     

        webServerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(JMeterUtils.getResString("web_server")));

        serverLabel.setText(JMeterUtils.getResString("web_server_domain"));

        portLabel.setText(JMeterUtils.getResString("web_server_port"));

        javax.swing.GroupLayout webServerPanelLayout = new javax.swing.GroupLayout(webServerPanel);
        webServerPanel.setLayout(webServerPanelLayout);
        webServerPanelLayout.setHorizontalGroup(
        	webServerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(webServerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(serverLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(server)
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
                    .addComponent(serverLabel)
                    .addComponent(server, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(portLabel)
                    .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        timeoutPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(JMeterUtils.getResString("timeout_title")));

        connectTimeoutLabel.setText(JMeterUtils.getResString("web_server_timeout_connect"));


        javax.swing.GroupLayout timeoutPanelLayout = new javax.swing.GroupLayout(timeoutPanel);
        timeoutPanel.setLayout(timeoutPanelLayout);
        timeoutPanelLayout.setHorizontalGroup(
    		timeoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(timeoutPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(connectTimeoutLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(connectionTimeout)
                .addContainerGap())
        );
        timeoutPanelLayout.setVerticalGroup(
        	timeoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(timeoutPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(timeoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(connectTimeoutLabel)
                    .addComponent(connectionTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        requestPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("WebSocket Request"));

        protocolLabel.setText("Protocol [ws/wss]:");

        pathLabel.setText(JMeterUtils.getResString("path"));

        contentEncodingLabel.setText(JMeterUtils.getResString("content_encoding"));

        connectionIdLabel.setText("Connection Id:");

        querystringAttributesPanel.setLayout(new javax.swing.BoxLayout(querystringAttributesPanel, javax.swing.BoxLayout.LINE_AXIS));

        implementationLabel.setText(JMeterUtils.getResString("http_implementation"));

        implementationComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Jetty" }));

        javax.swing.GroupLayout requestPanelLayout = new javax.swing.GroupLayout(requestPanel);
        requestPanel.setLayout(requestPanelLayout);
		requestPanelLayout.setHorizontalGroup(
    		requestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(requestPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(requestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(querystringAttributesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(requestPanelLayout.createSequentialGroup()
                        .addComponent(implementationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(implementationComboBox, 0, 1, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(protocolLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(protocol, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(contentEncodingLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(contentEncoding, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(connectionIdLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(connectionId))
                    .addGroup(requestPanelLayout.createSequentialGroup()
                        .addComponent(pathLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(path)))
                .addContainerGap())
        );
		requestPanelLayout.setVerticalGroup(
			requestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(requestPanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(requestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(protocolLabel)
                    .addComponent(protocol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contentEncodingLabel)
                    .addComponent(contentEncoding, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(connectionIdLabel)
                    .addComponent(connectionId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(implementationLabel)
                    .addComponent(implementationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(requestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pathLabel)
                    .addComponent(path, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(querystringAttributesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                .addGap(8, 8, 8)
                .addContainerGap())
        );
		
		responsePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("WebSocket Response"));
		closePatternLabel.setText("Close Connection Pattern");
		
		javax.swing.GroupLayout responsePanelLayout = new javax.swing.GroupLayout(responsePanel);
		responsePanel.setLayout(responsePanelLayout);
		responsePanelLayout.setHorizontalGroup(
				responsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(responsePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(responsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            		.addGroup(responsePanelLayout.createSequentialGroup()
                            .addComponent(closePatternLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(closePattern))
            		.addComponent(querystringPatternsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
		
		responsePanelLayout.setVerticalGroup(
				responsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(responsePanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(responsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closePatternLabel)
                    .addComponent(closePattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(querystringPatternsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                .addGap(8, 8, 8)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(requestPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(responsePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(webServerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(timeoutPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
                .addComponent(requestPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	            .addComponent(responsePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }

    public void initFields() {
    }

    public void setConnectionId(String connectionIdParam) {
    	connectionId.setText(connectionIdParam);
    }

    public String getConnectionId() {
        return connectionId.getText();
    }

    public void setContentEncoding(String contentEncodingParam) {
        contentEncoding.setText(contentEncodingParam);
    }

    public String getContentEncoding() {
        return contentEncoding.getText();
    }

    public void setPath(String contextPathParam) {
        path.setText(contextPathParam);
    }

    public String getPath() {
        return path.getText();
    }

    public void setProtocol(String protocolParam) {
        protocol.setText(protocolParam);
    }

    public String getProtocol() {
        return protocol.getText();
    }

    public void setConnectionTimeout(String connectionTimeoutParam) {
        connectionTimeout.setText(connectionTimeoutParam);
    }

    public String getConnectionTimeout() {
        return connectionTimeout.getText();
    }

    public void setServer(String serverAddressParam) {
        server.setText(serverAddressParam);
    }

    public String getServer() {
        return server.getText();
    }

    public void setPort(String serverPortParam) {
        port.setText(serverPortParam);
    }

    public String getPort() {
        return port.getText();
    }

    public void setImplementation(String implementationParam) {
        implementationComboBox.setSelectedItem(implementationParam);
    }

    public String getImplementation() {
        return (String) implementationComboBox.getSelectedItem();
    }

    /**
     * @return the attributePanel
     */
    public ArgumentsPanel getAttributePanel() {
        return attributePanel;
    }
    
    public ArgumentsPanel getPatternsPanel() {
        return patternsPanel;
    }


	public void setCloseConnectionPattern(String closeConnectionPattern) {
		closePattern.setText(closeConnectionPattern);
		
	}


	public String getCloseConnectionPattern() {
		return closePattern.getText();
	}

}
