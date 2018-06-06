package com.blazemeter.jmeter.http2.sampler;

import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.frames.DataFrame;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.FuturePromise;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTP2Connection {

    private static Logger LOG = LoggerFactory.getLogger(HTTP2Connection.class);
    private String connectionId;
    private Session session;
    private HTTP2Client client;
    private SslContextFactory sslContextFactory;
    private Queue<HTTP2StreamHandler> streamHandlers = new ConcurrentLinkedQueue<>();

    public void setSession(Session session) {
        this.session = session;
    }

    public HTTP2Connection(String connectionId, boolean isSSL) throws Exception {
        this.session = null;
        this.connectionId = connectionId;
        this.client = new HTTP2Client();
        this.sslContextFactory = null;
        if (isSSL) {
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
        this.client.connect(this.sslContextFactory, new InetSocketAddress(hostname, port),
                new Session.Listener.Adapter(), sessionFuture);
        setSession(sessionFuture.get(10, TimeUnit.SECONDS));
    }

    public boolean isClosed() {
        return this.session.isClosed();
    }

    private synchronized void sendMutExc(String method, HeadersFrame headersFrame, FuturePromise<Stream> streamPromise,
                                         HTTP2StreamHandler http2StreamHandler, RequestBody requestBody) throws Exception {
        session.newStream(headersFrame, streamPromise, http2StreamHandler);
        if (HTTPConstants.POST.equals(method)) {
            Stream actualStream = streamPromise.get();
            int streamID = actualStream.getId();
            DataFrame data = new DataFrame(streamID, ByteBuffer.wrap(requestBody.getPayloadBytes()), true);
            actualStream.data(data, Callback.NOOP);
        }
    }

    public void send(String method, URL url, HeaderManager headerManager, CookieManager cookieManager,
                     RequestBody requestBody, HTTP2SampleResult sampleResult, int timeout) throws Exception {
        HttpFields headers = buildHeaders(url, headerManager, cookieManager);

        if (requestBody != null) {
            headers.put(HTTPConstants.HEADER_CONTENT_LENGTH, Long.toString(requestBody.getPayloadBytes().length));
            // Check if the header manager had a content type header
            // This allows the user to specify his own content-type for a POST request
            String contentTypeHeader = headers.get(HTTPConstants.HEADER_CONTENT_TYPE);
            if (contentTypeHeader == null || contentTypeHeader.isEmpty()) {
                headers.put(HTTPConstants.HEADER_CONTENT_TYPE, HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED);
            }
            sampleResult.setQueryString(requestBody.getPayload());
        }

        MetaData.Request metaData = null;
        boolean endOfStream = true;
        switch (method) {
            case "GET":
                metaData = new MetaData.Request("GET", new HttpURI(url.toString()), HttpVersion.HTTP_2,
                        headers);
                break;
            case "POST":
                metaData = new MetaData.Request("POST", new HttpURI(url.toString()), HttpVersion.HTTP_2,
                        headers);
                endOfStream = false;
                break;
            default:
                break;
        }

        HeadersFrame headersFrame = new HeadersFrame(metaData, null, endOfStream);
        // we do this replacement and remove final char to be consistent with jmeter HTTP request sampler
        String headersString = headers.toString().replaceAll("\r\n", "\n");
        sampleResult.setRequestHeaders(headersString.substring(0, headersString.length() - 1));

        HTTP2StreamHandler http2StreamHandler = new HTTP2StreamHandler(this, url, headerManager, cookieManager,
                sampleResult);
        http2StreamHandler.setTimeout(timeout);
        sampleResult.setCookies(headers.get(HTTPConstants.HEADER_COOKIE));
        addStreamHandler(http2StreamHandler);

        sampleResult.sampleStart();

        sendMutExc(method, headersFrame, new FuturePromise<>(), http2StreamHandler, requestBody);
    }

    private HttpFields buildHeaders(URL url, HeaderManager headerManager, CookieManager cookieManager) {
        HttpFields headers = new HttpFields();
        if (headerManager != null) {
            CollectionProperty headersProps = headerManager.getHeaders();
            if (headersProps != null) {
                for (JMeterProperty prop : headersProps) {
                    Header header = (Header) prop.getObjectValue();
                    String n = header.getName();
                    // Don't allow override of Content-Length
                    // TODO - what other headers are not allowed?
                    if(n.contains(":")){
                        LOG.warn("The specified pseudo header {} is not allowed "
                            + "and will be ignored", n);
                    }
                    else if (!HTTPConstants.HEADER_CONTENT_LENGTH.equalsIgnoreCase(n)) {
                        String v = header.getValue();
                        v = v.replaceFirst(":\\d+$", ""); // remove any port
                        headers.put(n, v);
                    }
                }
            }
            // TODO CacheManager
        }
        if (cookieManager != null) {
            String cookieHeader = cookieManager.getCookieHeaderForURL(url);
            if (cookieHeader != null) {
                headers.put(HTTPConstants.HEADER_COOKIE, cookieHeader);
            }
        }

        return headers;
    }

    public void addStreamHandler(HTTP2StreamHandler http2StreamHandler) {
        streamHandlers.add(http2StreamHandler);
    }

    public void disconnect() throws Exception {
        client.stop();
    }

    public List<HTTP2SampleResult> awaitResponses() throws InterruptedException {
        List<HTTP2SampleResult> results = new ArrayList<>();
        while (!streamHandlers.isEmpty()) {
            HTTP2StreamHandler h = streamHandlers.poll();
            results.add(h.getHTTP2SampleResult());
            try {
                // wait to receive all the response of the request
                h.getCompletedFuture().get(h.getTimeout(), TimeUnit.MILLISECONDS);
            } catch (ExecutionException | TimeoutException e) {
                HTTP2SampleResult sample = h.getHTTP2SampleResult();
                sample.setErrorResult("Error while await for response", e);
                sample.setResponseHeaders("");
            }
        }
        return results;
    }

}
