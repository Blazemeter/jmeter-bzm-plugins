package com.blazemeter.jmeter.hls.gui;

import com.blazemeter.jmeter.hls.logic.HlsSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.awt.BorderLayout;

public class HlsSamplerGui extends AbstractSamplerGui {
	private static final Logger log = LoggingManager.getLoggerForClass();
	private HlsSamplerPanel hlsSamplerPanel;

	public HlsSamplerGui() {
		super();
		init();
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());

		this.add(makeTitlePanel(), BorderLayout.NORTH);
		this.add(hlsSamplerPanel, BorderLayout.CENTER);
	}

	public String getStaticLabel() {
		return "HLS Sampler";
	}

	public String getLabelResource() {
		return "HLS Sampler";
	}

	public TestElement createTestElement() {
		HlsSampler sampler = new HlsSampler();
		modifyTestElement(sampler);
		return sampler;
	}

	public void configure(TestElement el) {
		super.configure(el);
		HlsSampler sampler = (HlsSampler) el;
		hlsSamplerPanel.setUrlData(sampler.getURLData());
		hlsSamplerPanel.setResData(sampler.getRESDATA());
		hlsSamplerPanel.setPlaySecondsData(sampler.getPlAYSecondsData());
		hlsSamplerPanel.setVideoDuration(sampler.getVideoDuration());
		hlsSamplerPanel.setVideoType(sampler.getVideoType());
		hlsSamplerPanel.setProtocol(sampler.getPRotocol());
		hlsSamplerPanel.setNetData(sampler.getNetwordData());
		hlsSamplerPanel.setResolutionType(sampler.getResolutionType());
		hlsSamplerPanel.setBandwidthType(sampler.getBandwidthType());

	}

	public void modifyTestElement(TestElement s) {
		this.configureTestElement(s);
		if (s instanceof HlsSampler) {
			HlsSampler sampler = (HlsSampler) s;
			sampler.setURLData(hlsSamplerPanel.getUrlData());
			sampler.setResData(hlsSamplerPanel.getResData());
			sampler.setPlaySecondsData(hlsSamplerPanel.getPlaySecondsData());
			sampler.setHlsDuration(hlsSamplerPanel.isChecked());
			sampler.setHlsVideoType(hlsSamplerPanel.videoType());
			sampler.setVideoDuration(hlsSamplerPanel.getVideoDuration());
			sampler.setPRotocol(hlsSamplerPanel.getProtocol());
			sampler.setNetworkData(hlsSamplerPanel.getNetData());
			sampler.setResolutionType(hlsSamplerPanel.getResolutionType());
			sampler.setBandwidthType(hlsSamplerPanel.getBandwidthType());
		}

	}

	public void clearGui() {
		super.clearGui();
		this.hlsSamplerPanel.setPlaySecondsData("");
		this.hlsSamplerPanel.setResData("");
		this.hlsSamplerPanel.setUrlData("");
		this.hlsSamplerPanel.setNetData("");

	}

	private void init() {
		hlsSamplerPanel = new HlsSamplerPanel();

	}
}
