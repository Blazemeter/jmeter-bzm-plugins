package com.blazemeter.jmeter.hls.logic;

import org.junit.BeforeClass;
import org.junit.Test;

import kg.apc.emulators.TestJMeterUtils;

public class HlsSamplerTest {
	
	
	@BeforeClass
	public static void setUpClass()
	        throws Exception {
        TestJMeterUtils.createJmeterEnv();
	}
	
	@Test
	public void rotatingRules() throws Exception {
		
		HlsSampler hlsSampler = new HlsSampler();
	 
//		DataRequest getMasterList(SampleResult masterResult, Parser parser);
	
	}

}
