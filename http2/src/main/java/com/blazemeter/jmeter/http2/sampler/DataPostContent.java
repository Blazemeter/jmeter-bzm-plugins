package com.blazemeter.jmeter.http2.sampler;

public class DataPostContent {

	String dataPath = "";
	byte[] payload = null;

	public String getDataPath() {
		return dataPath;
	}

	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	public byte[] getPayload() {
		return payload;
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
	}

}
