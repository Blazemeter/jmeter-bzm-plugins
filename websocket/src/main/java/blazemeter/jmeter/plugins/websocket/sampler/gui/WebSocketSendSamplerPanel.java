
package blazemeter.jmeter.plugins.websocket.sampler.gui;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;


public class WebSocketSendSamplerPanel extends javax.swing.JPanel {
	
	private JPanel requestPanel = new JPanel();
	private JEditorPane payloadContent = new JEditorPane();
	private JScrollPane bodyPanel = new JScrollPane();
	private JCheckBox waitUntilResponse = new JCheckBox();
	private JLabel responsePatternLabel = new JLabel();
	private JTextField responsePattern = new JTextField();
	private JLabel responseTimeoutLabel = new JLabel();
	private JTextField responseTimeout = new JTextField();
	
    
    public WebSocketSendSamplerPanel() {
        initComponents();
    }


    private void initComponents() {
  
        requestPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("WebSocket Message"));
        
        responseTimeoutLabel.setText("Response Timeout (ms): ");
        responsePatternLabel.setText("Response Pattern: ");
        waitUntilResponse.setText("Wait Until Response");
        
        bodyPanel.setViewportView(payloadContent);

        javax.swing.GroupLayout requestPanelLayout = new javax.swing.GroupLayout(requestPanel);
        requestPanel.setLayout(requestPanelLayout);
		requestPanelLayout.setHorizontalGroup(
    		requestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(requestPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bodyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
                .addGroup(requestPanelLayout.createSequentialGroup()
                		.addContainerGap()
                		.addComponent(responsePatternLabel)
                		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            			.addComponent(responsePattern)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(waitUntilResponse)
                        .addContainerGap())
                .addGroup(requestPanelLayout.createSequentialGroup()
                		.addContainerGap()
                		.addComponent(responseTimeoutLabel)
                		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            			.addComponent(responseTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
            			.addContainerGap())
        );
		requestPanelLayout.setVerticalGroup(
			requestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(requestPanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(bodyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(requestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(responsePattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responsePatternLabel)
                	.addComponent(waitUntilResponse))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(requestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(responseTimeoutLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(responseTimeout))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
                    .addComponent(requestPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
	            .addComponent(requestPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE)
	            .addContainerGap())
        );
    }

    public void initFields() {
    	payloadContent.setText("");
    	waitUntilResponse.setSelected(false);
    	responsePattern.setText("");
    	responseTimeout.setText("");
    }

	public void setPayloadContent(String payloadContent2) {
		this.payloadContent.setText(payloadContent2);
	}


	public void setWaitUntilResponse(boolean waitUntilResponse2) {
		this.waitUntilResponse.setSelected(waitUntilResponse2);
	}


	public void setResponsePattern(String responsePattern2) {
		this.responsePattern.setText(responsePattern2);
	}


	public void setResponseTimeout(String responseTimeout2) {
		this.responseTimeout.setText(responseTimeout2);
	}

	public boolean getWaitUntilResponse() {
		return this.waitUntilResponse.isSelected();
	}


	public String getPayloadContent() {
		return this.payloadContent.getText();
	}


	public String getResponsePattern() {
		return this.responsePattern.getText();
	}


	public String getResponseTimeout() {
		return this.responseTimeout.getText();
	}
}
