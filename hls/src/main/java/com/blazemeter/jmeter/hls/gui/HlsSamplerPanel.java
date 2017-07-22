package com.blazemeter.jmeter.hls.gui;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class HlsSamplerPanel extends JPanel {

	private JPanel videoPanel = new JPanel();
	private JPanel playOptions = new JPanel();
	private JPanel networkOptions = new JPanel();

	private JLabel urlFieldLabel = new JLabel("URL  ");
	private JLabel resolLabel = new JLabel(" Resolution :");
	private JLabel bitsPerSecond = new JLabel("bits / s");
	private JLabel protocol = new JLabel("Protocol");
	private JLabel bandwidthLabel = new JLabel(" Bandwidth:");

	private JTextField urlField = new JTextField();
	private JTextField resolField = new JTextField();
	private JTextField playSecondsField = new JTextField();
	private JTextField bitsField = new JTextField();
	private JTextField bandwidthField = new JTextField();

	JRadioButton rPlayVideoBtn = new JRadioButton("Whole video", true);
	JRadioButton rPlayPartBtn = new JRadioButton("Video duration (seconds):");
	JRadioButton rAutoBtn = new JRadioButton("Auto (maximum bandwidth available)");
	JRadioButton rManualBtn = new JRadioButton("Manual ");
	JRadioButton rVodStream = new JRadioButton("VOD", true);
	JRadioButton rliveStream = new JRadioButton("Live Stream");
	JRadioButton rEventStream = new JRadioButton("Event Stream");
	JRadioButton rCustomResol = new JRadioButton("Custom Resolution: ");
	JRadioButton rMaximumResol = new JRadioButton("Max resolution available");
	JRadioButton rMinimumResol = new JRadioButton("Min resolution available", true);
	JRadioButton rCustomBandwidth = new JRadioButton("Custom Bandwidth: ");
	JRadioButton rMaximumBandwidth = new JRadioButton("Max bandwidth available");
	JRadioButton rMinimumBandwidth = new JRadioButton("Min bandwidth available", true);

	ButtonGroup group = new ButtonGroup();
	ButtonGroup group2 = new ButtonGroup();
	ButtonGroup resolGroup = new ButtonGroup();
	ButtonGroup bandGroup = new ButtonGroup();

	String[] protocolType = { "https", "http" };
	String[] networksType = { "CUSTOM", "GPRS", "EDGE", "LTE", "WIFI 802.11a/g" };
	JComboBox jNetworkCombo = new JComboBox(networksType);
	JComboBox<String> jProtocolCombo = new JComboBox(protocolType);

	public HlsSamplerPanel() {
		initComponents();
	}

	private void initComponents() {

		group.add(rPlayVideoBtn);
		group.add(rPlayPartBtn);
		group2.add(rVodStream);
		group2.add(rliveStream);
		group2.add(rEventStream);

		resolGroup.add(rCustomResol);
		resolGroup.add(rMaximumResol);
		resolGroup.add(rMinimumResol);

		bandGroup.add(rCustomBandwidth);
		bandGroup.add(rMinimumBandwidth);
		bandGroup.add(rMaximumBandwidth);

		playSecondsField.setEnabled(false);
		rPlayPartBtn.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					playSecondsField.setEnabled(true);
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					playSecondsField.setText("");
					playSecondsField.setEnabled(false);
				}
				validate();
				repaint();
			}
		});
		bandwidthField.setEnabled(false);
		rCustomBandwidth.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					bandwidthField.setEnabled(true);
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					bandwidthField.setText("");
					bandwidthField.setEnabled(false);
				}
				validate();
				repaint();
			}
		});
		resolField.setEnabled(false);
		rCustomResol.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					resolField.setEnabled(true);
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					resolField.setText("");
					resolField.setEnabled(false);
				}
				validate();
				repaint();
			}
		});

		videoPanel.setBorder(BorderFactory.createTitledBorder("Video"));

		GroupLayout videoPanelLayout = new javax.swing.GroupLayout(videoPanel);
		videoPanel.setLayout(videoPanelLayout);
		videoPanelLayout.setHorizontalGroup(videoPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(videoPanelLayout.createSequentialGroup().addContainerGap().addGroup(videoPanelLayout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(videoPanelLayout.createSequentialGroup().addComponent(urlFieldLabel).addContainerGap()
								.addComponent(urlField)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(rVodStream).addComponent(rliveStream).addComponent(rEventStream))

						)));
		videoPanelLayout.setVerticalGroup(videoPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(videoPanelLayout.createSequentialGroup().addContainerGap()
						.addGroup(videoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(urlFieldLabel)
								.addComponent(urlField, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(rVodStream).addComponent(rliveStream).addComponent(rEventStream))
						.addContainerGap())

				);

		playOptions.setBorder(BorderFactory.createTitledBorder("Play Options"));
		GroupLayout playOptionsLayout = new javax.swing.GroupLayout(playOptions);
		playOptions.setLayout(playOptionsLayout);
		playOptionsLayout.setHorizontalGroup(playOptionsLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(playOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(playOptionsLayout.createSequentialGroup().addComponent(rPlayVideoBtn))
						.addGroup(playOptionsLayout.createSequentialGroup().addComponent(rPlayPartBtn)
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(playSecondsField))
						.addGroup(playOptionsLayout.createSequentialGroup().addComponent(rCustomResol)
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(resolField))
						.addGroup(playOptionsLayout.createSequentialGroup().addComponent(rMinimumResol))
						.addGroup(playOptionsLayout.createSequentialGroup().addComponent(rMaximumResol))

						));
		playOptionsLayout.setVerticalGroup(playOptionsLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(playOptionsLayout.createSequentialGroup().addContainerGap()
						.addGroup(playOptionsLayout.createParallelGroup().addComponent(rPlayVideoBtn))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(playOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(rPlayPartBtn).addComponent(playSecondsField))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(playOptionsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(rCustomResol).addComponent(resolField))
						.addGroup(playOptionsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(rMinimumResol))
						.addGroup(playOptionsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(rMaximumResol))

						.addContainerGap())

				);

		networkOptions.setBorder(BorderFactory.createTitledBorder("Network Options"));
		GroupLayout networkOptionsLayout = new javax.swing.GroupLayout(networkOptions);
		networkOptions.setLayout(networkOptionsLayout);
		networkOptionsLayout
		.setHorizontalGroup(networkOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

				.addGroup(networkOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(networkOptionsLayout.createSequentialGroup()

								.addComponent(protocol).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jProtocolCombo))

						.addGroup(networkOptionsLayout.createSequentialGroup().addComponent(rCustomBandwidth)
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(bandwidthField)
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(bitsPerSecond))
						.addGroup(networkOptionsLayout.createSequentialGroup().addComponent(rMinimumBandwidth))

						.addGroup(networkOptionsLayout.createSequentialGroup().addComponent(rMaximumBandwidth))

						));
		networkOptionsLayout.setVerticalGroup(networkOptionsLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(networkOptionsLayout.createSequentialGroup().addContainerGap()
						.addGroup(networkOptionsLayout.createParallelGroup().addComponent(protocol, 25, 25, 25)
								.addComponent(jProtocolCombo))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(networkOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(rCustomBandwidth).addComponent(bandwidthField)
								.addComponent(bitsPerSecond))// ,25,25,25))
						.addGroup(networkOptionsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(rMinimumBandwidth))
						.addGroup(networkOptionsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(rMaximumBandwidth))
						.addContainerGap().addGap(35, 35, 35)));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(videoPanel, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(layout.createSequentialGroup()
										.addComponent(playOptions, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(networkOptions, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
						.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(videoPanel, javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE)// Short.MAX_VALUE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(networkOptions, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(playOptions, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addContainerGap())

				);

	}

	public void setUrlData(String urlData) {
		urlField.setText(urlData);
	}

	public String getUrlData() {
		return urlField.getText();
	}

	public void setResData(String resData) {
		resolField.setText(resData);
	}

	public String getResData() {
		return resolField.getText();
	}

	public void setNetData(String netData) {
		bandwidthField.setText(netData);
	}

	public String getNetData() {
		return bandwidthField.getText();
	}

	public void setPlaySecondsData(String seconds) {
		playSecondsField.setText(seconds);
	}

	public String getPlaySecondsData() {
		return playSecondsField.getText();
	}

	public void setVideoDuration(boolean check) {
		rPlayPartBtn.setSelected(check);
	}

	public boolean getVideoDuration() {
		return rPlayPartBtn.isSelected();
	}

	public void setVideoType(String check) {

		if (check.equalsIgnoreCase("event"))
			rEventStream.setSelected(true);
		else if (check.equalsIgnoreCase("live"))
			rliveStream.setSelected(true);
		else
			rVodStream.setSelected(true);
	}

	public void setResolutionType(String check) {
		if (check.equalsIgnoreCase("minResolution"))
			rMinimumResol.setSelected(true);
		else if (check.equalsIgnoreCase("maxResolution"))
			rMaximumResol.setSelected(true);
		else
			rCustomResol.setSelected(true);

	}

	public void setBandwidthType(String check) {
		if (check.equalsIgnoreCase("minBandwidth"))
			rMinimumBandwidth.setSelected(true);
		else if (check.equalsIgnoreCase("maxBandwidth"))
			rMaximumBandwidth.setSelected(true);
		else
			rCustomBandwidth.setSelected(true);

	}

	public void setProtocol(String protocolValue) {
		jProtocolCombo.setSelectedItem(protocolValue);
	}

	public String getProtocol() {
		return jProtocolCombo.getSelectedItem().toString();
	}

	public String isChecked() {
		if (rPlayVideoBtn.isSelected()) {

			return "-1";

		} else {

			return getPlaySecondsData();
		}
	}

	public String getResolutionType() {
		if (rCustomResol.isSelected()) {
			return "customResolution";
		} else if (rMinimumResol.isSelected()) {
			return "minResolution";
		} else
			return "maxResolution";
	}

	public String getBandwidthType() {
		if (rCustomBandwidth.isSelected()) {
			return "customBandwidth";
		} else if (rMinimumBandwidth.isSelected()) {
			return "minBandwidth";
		} else
			return "maxBandwidth";
	}

	public String videoType() {
		if (rVodStream.isSelected()) {
			return "vod";
		} else if (rliveStream.isSelected()) {
			return "live";
		} else
			return "event";
	}

	public boolean rDurationVideoButtoncheck() {
		return rPlayVideoBtn.isSelected();
	}

}
