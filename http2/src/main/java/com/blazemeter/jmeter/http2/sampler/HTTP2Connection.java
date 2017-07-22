package com.blazemeter.jmeter.http2.sampler;

import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.eclipse.jetty.http.*;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.frames.DataFrame;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.util.FuturePromise;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class HTTP2Connection {

	String SETTINGS = "settings";

	private String connectionId;
	private Session session;
	private boolean isSSL;
	private HTTP2Client client;
	private HTTP2SettingsHandler http2SettingsHandler;
	private SslContextFactory sslContextFactory;
	private Map<Integer, HTTP2SampleResult> pendingResponses = new HashMap<Integer, HTTP2SampleResult>();
	private Map<Integer, HTTP2StreamHandler> streamHandlers = new ConcurrentHashMap<>();
	private List<DataCallBack> callbackHandler = new ArrayList<DataCallBack>();
	private int frameSize = 1024;

	public synchronized Collection<HTTP2StreamHandler> addPendingResponses(HTTP2SampleResult pendingResponse,
			HTTP2StreamHandler streamHandler, boolean isSec) {
		if (!((pendingResponse == null) && (streamHandler == null))) {
			if (!isSec) {
				this.pendingResponses.put(pendingResponse.getId(), pendingResponse);
			}
			this.streamHandlers.put(pendingResponse.getId(), streamHandler);
			return null;
		} else {
			return this.streamHandlers.values();
		}
	}


	public void setFrameSize(int frameSize) {
		this.frameSize = frameSize;
	}

	public Map<Integer, HTTP2SampleResult> getPendingResponses() {
		return this.pendingResponses;
	}
	
	public void setSession(Session session){
		this.session = session;
	}

	public HTTP2Connection(String connectionId, boolean isSSL) throws Exception {
		this.session = null;
		this.connectionId = connectionId;
		this.isSSL = isSSL;
		this.http2SettingsHandler = new HTTP2SettingsHandler(this);
		this.client = new HTTP2Client();
		this.sslContextFactory = null;
		if (this.isSSL) {
			this.sslContextFactory = new SslContextFactory(true);
		}
		this.client.addBean(sslContextFactory);
		this.client.start();
	}

	public String getConnectionId() {
		return connectionId;
	}

	public void connect(String hostname, int port) throws InterruptedException, ExecutionException, TimeoutException {
		FuturePromise<Session> sessionFuture = new FuturePromise<>();
		this.client.connect(this.sslContextFactory, new InetSocketAddress(hostname, port), this.http2SettingsHandler,
				sessionFuture);
		setSession(sessionFuture.get(10, TimeUnit.SECONDS));
	}

	public boolean isClosed() {
		return this.session.isClosed();
	}

	private synchronized void sendMutExc(String method, HeadersFrame headersFrame, FuturePromise<Stream> streamPromise,
			HTTP2StreamHandler http2StreamHandler, DataPostContent dataPostContent, HTTP2SampleResult sampleResult)
			throws Exception {
		session.newStream(headersFrame, streamPromise, http2StreamHandler);
		if (method.equals("POST")) {
			Stream actualStream = streamPromise.get();
			int streamID = actualStream.getId();
			DataCallBack dataCallback = new DataCallBack();
			DataFrame data = new DataFrame(streamID,
					ByteBuffer.wrap(dataPostContent.getPayload(), 0, dataPostContent.getPayload().length), true);
			actualStream.data(data, dataCallback);
			this.addDataCallbackHandler(dataCallback);
			sampleResult.setQueryString(data.toString());// TODO revisar si es este metodo
			// add byte size of the queryString
			sampleResult.setBytes(sampleResult.getBytesAsLong() + (long) sampleResult.getQueryString().length()); 
		}

	}

	public void addDataCallbackHandler(DataCallBack dataCallback) {
		callbackHandler.add(dataCallback);
	}


	public void send(String method, URL url, HeaderManager headerManager, CookieManager cookieManager,
			DataPostContent dataPostContent, HTTP2SampleResult sampleResult, boolean secondaryRequest, int timeout)
			throws Exception {

		HttpFields requestFields = new HttpFields();

		String headerString = "";

		if (headerManager != null) {
			CollectionProperty headers = headerManager.getHeaders();
			if (headers != null) {
				for (JMeterProperty jMeterProperty : headers) {
					org.apache.jmeter.protocol.http.control.Header header = (org.apache.jmeter.protocol.http.control.Header) jMeterProperty
							.getObjectValue();
					String n = header.getName();
					// Don't allow override of Content-Length
					// TODO - what other headers are not allowed?
					if (!HTTPConstants.HEADER_CONTENT_LENGTH.equalsIgnoreCase(n)) {
						String v = header.getValue();
						v = v.replaceFirst(":\\d+$", ""); // remove any port
															// specification //
															// $NON-NLS-1$
															// $NON-NLS-2$
						requestFields.put(n, v);
						headerString = headerString + n + ": " + v + "\n";
					}
				}
			}
			// TODO CacheManager
		}

		sampleResult.sampleStart();

		// Extracts all the required cookies for that particular URL request
		String cookieHeader = null;
		if (cookieManager != null) {
			cookieHeader = cookieManager.getCookieHeaderForURL(url);
			if (cookieHeader != null) {
				requestFields.put(HTTPConstants.HEADER_COOKIE, cookieHeader);
				headerString = headerString + HTTPConstants.HEADER_COOKIE + ": " + cookieHeader + "\n";
			}
		}

		MetaData.Request metaData = null;
		boolean endOfStream = true;
		switch (method) {
			case "GET":
				metaData = new MetaData.Request("GET", new HttpURI(url.toString()), HttpVersion.HTTP_2, requestFields);
				break;
			case "HEAD":
				break;
			case "POST":
				metaData = new MetaData.Request("POST", new HttpURI(url.toString()), HttpVersion.HTTP_2, requestFields);
				endOfStream = false;
				break;
			case "PUT":
				break;
			case "DELETE":
				break;
			case "CONNECT":
				break;
			case "OPTIONS":
				break;
			case "TRACE":
				break;
			case "PATCH":
				break;
			default:
				break;
		}

		HeadersFrame headersFrame = new HeadersFrame(metaData, null, endOfStream);
		sampleResult.setRequestHeaders(headerString);
		sampleResult.setBytes(sampleResult.getBytesAsLong() + (long) headerString.length());

		FuturePromise<Stream> streamPromise = new FuturePromise<>();

		HTTP2StreamHandler http2StreamHandler = new HTTP2StreamHandler(this, url, headerManager, cookieManager, false, sampleResult);
		http2StreamHandler.setTimeout(timeout);
		sampleResult.setCookies(cookieHeader);
		addPendingResponses(sampleResult, http2StreamHandler, secondaryRequest);

		sendMutExc(method, headersFrame, streamPromise, http2StreamHandler, dataPostContent, sampleResult);
	}

	public void disconnect() throws Exception {
		client.stop();
	}

	public void sync() throws InterruptedException {

		for (DataCallBack d : this.callbackHandler) {
			try {
				d.getCompletedFuture().get();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		int check = -1;
		Collection<HTTP2StreamHandler> handlers = addPendingResponses(null, null, true);
		int size = handlers.size();
		while (size != check) {
			for (HTTP2StreamHandler h : handlers) {
				try {
					// wait to receive all the response of the request
					h.getCompletedFuture().get(h.getTimeout(), TimeUnit.MILLISECONDS);
				} catch (ExecutionException | TimeoutException e) {
					// TODO Auto-generated catch block
					HTTP2SampleResult sample = h.getHTTP2SampleResult();
					// remove the request that received timeout
					pendingResponses.remove(sample.getId());  
					streamHandlers.remove(sample.getId());
					if (e instanceof TimeoutException) {
						sample = HTTP2SampleResult.errorResult(e, sample);
						sample.setResponseHeaders("");
					}

				}
			}
			check = size;
			handlers = addPendingResponses(null, null, true);
			size = handlers.size();
		}
	}

}
