
package blazemeter.jmeter.plugins.websocket.sampler.gui;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class WebSocketCollectorSamplerPanel extends javax.swing.JPanel {
    

    /**
	 * 
	 */
	private static final long serialVersionUID = -5724312753869984987L;
	
	private JPanel collectorPanel = new JPanel();
	private JCheckBox waitUntilMessage = new JCheckBox();
	private JCheckBox closeConnection = new JCheckBox();
	private JLabel responsePatternLabel = new JLabel();
	private JTextField responsePattern = new JTextField();
	private JLabel responseTimeoutLabel = new JLabel();
	private JTextField responseTimeout = new JTextField();
	
    
    public WebSocketCollectorSamplerPanel() {
        initComponents();
    }


    private void initComponents() {

        collectorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("WebSocket Message"));
        
        responseTimeoutLabel.setText("Response Timeout (ms): ");
        responsePatternLabel.setText("Response Pattern: ");
        waitUntilMessage.setText("Wait Until Message");
        closeConnection.setText("Close Connection");
        

        javax.swing.GroupLayout collectorPanelLayout = new javax.swing.GroupLayout(collectorPanel);
        collectorPanel.setLayout(collectorPanelLayout);
        collectorPanelLayout.setHorizontalGroup(
    		collectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(collectorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(collectorPanelLayout.createSequentialGroup()
                		.addContainerGap()
                		.addComponent(closeConnection)
                		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(waitUntilMessage)
                        .addContainerGap())
                .addContainerGap())
                .addGroup(collectorPanelLayout.createSequentialGroup()
                		.addContainerGap()
                		.addComponent(responsePatternLabel)
                		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            			.addComponent(responsePattern)
                        .addContainerGap())
                .addGroup(collectorPanelLayout.createSequentialGroup()
                		.addContainerGap()
                		.addComponent(responseTimeoutLabel)
                		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            			.addComponent(responseTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
            			.addContainerGap())
        );
        collectorPanelLayout.setVerticalGroup(
    		collectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(collectorPanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(collectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                		.addComponent(closeConnection)
                    	.addComponent(waitUntilMessage))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(collectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(responsePattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responsePatternLabel)
                	.addComponent(waitUntilMessage))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(collectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
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
                    .addComponent(collectorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
	            .addComponent(collectorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE)
	            .addContainerGap())
        );
    }

    public void initFields() {
    	waitUntilMessage.setSelected(false);
    	closeConnection.setSelected(false);
    	responsePattern.setText("");;
    	responseTimeout.setText("");;
    }

	public void setWaitUntilMessage(boolean waitUntilMessage2) {
		this.waitUntilMessage.setSelected(waitUntilMessage2);
	}

	public void setCloseConnection(boolean closeConnection2) {
		this.closeConnection.setSelected(closeConnection2);
	}

	public void setResponsePattern(String responsePattern2) {
		this.responsePattern.setText(responsePattern2);
	}


	public void setResponseTimeout(String responseTimeout2) {
		this.responseTimeout.setText(responseTimeout2);
	}

	public boolean getWaitUntilResponse() {
		return this.waitUntilMessage.isSelected();
	}

	public boolean getCloseConnection() {
		return this.closeConnection.isSelected();
	}

	public String getResponsePattern() {
		return this.responsePattern.getText();
	}


	public String getResponseTimeout() {
		return this.responseTimeout.getText();
	}
}
