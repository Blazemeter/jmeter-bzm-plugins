package com.blazemeter.jmeter.http2.sampler;

import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.frames.SettingsFrame;

public class HTTP2SettingsHandler extends Session.Listener.Adapter {

	private HTTP2Connection connection = null;

	public HTTP2SettingsHandler(HTTP2Connection _connection) {
		// TODO Auto-generated constructor stub
		this.connection = _connection;
	}

	@Override
	public void onSettings(Session session, SettingsFrame frame) {
		super.onSettings(session, frame);
		connection.setFrameSize(frame.MAX_FRAME_SIZE);
	}

}