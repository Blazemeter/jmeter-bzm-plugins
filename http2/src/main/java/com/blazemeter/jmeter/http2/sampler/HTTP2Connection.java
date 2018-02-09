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
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.eclipse.jetty.util.FuturePromise;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class HTTP2Connection {

    private String connectionId;
    private Session session;
    private HTTP2Client client;
    private SslContextFactory sslContextFactory;
    private Set<HTTP2StreamHandler> streamHandlers = new ConcurrentHashSet<>();

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
                                         HTTP2StreamHandler http2StreamHandler, DataPostContent dataPostContent,
                                         HTTP2SampleResult sampleResult) throws Exception {
        session.newStream(headersFrame, streamPromise, http2StreamHandler);
        if (method.equals("POST")) {
            Stream actualStream = streamPromise.get();
            int streamID = actualStream.getId();
            DataFrame data = new DataFrame(streamID,
                    ByteBuffer.wrap(dataPostContent.getPayload(), 0, dataPostContent.getPayload().length), true);
            actualStream.data(data, null);
            sampleResult.setQueryString(data.toString());// TODO review this method
            // add byte size of the queryString
            sampleResult.setBytes(sampleResult.getBytesAsLong() + (long) sampleResult.getQueryString().length());
        }
    }

    public void send(String method, URL url, HeaderManager headerManager, CookieManager cookieManager,
                     DataPostContent dataPostContent, HTTP2SampleResult sampleResult, int timeout) throws Exception {
        HttpFields requestFields = new HttpFields();
        StringBuilder headerString = new StringBuilder();
        if (headerManager != null) {
            CollectionProperty headers = headerManager.getHeaders();
            if (headers != null) {
                for (JMeterProperty jMeterProperty : headers) {
                    Header header = (Header) jMeterProperty.getObjectValue();
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
                        headerString.append(n).append(": ").append(v).append("\n");
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
                headerString.append(HTTPConstants.HEADER_COOKIE).append(": ").append(cookieHeader).append("\n");
            }
        }

        MetaData.Request metaData = null;
        boolean endOfStream = true;
        switch (method) {
            case "GET":
                metaData = new MetaData.Request("GET", new HttpURI(url.toString()), HttpVersion.HTTP_2,
                        requestFields);
                break;
            case "POST":
                metaData = new MetaData.Request("POST", new HttpURI(url.toString()), HttpVersion.HTTP_2,
                        requestFields);
                endOfStream = false;
                break;
            default:
                break;
        }

        HeadersFrame headersFrame = new HeadersFrame(metaData, null, endOfStream);
        sampleResult.setRequestHeaders(headerString.toString());
        sampleResult.setBytes(sampleResult.getBytesAsLong() + (long) headerString.length());

        HTTP2StreamHandler http2StreamHandler = new HTTP2StreamHandler(this, url, headerManager, cookieManager,
                sampleResult);
        http2StreamHandler.setTimeout(timeout);
        sampleResult.setCookies(cookieHeader);
        addStreamHandler(http2StreamHandler);

        sendMutExc(method, headersFrame, new FuturePromise<>(), http2StreamHandler, dataPostContent, sampleResult);
    }

    public void addStreamHandler(HTTP2StreamHandler http2StreamHandler) {
        streamHandlers.add(http2StreamHandler);
    }

    public void disconnect() throws Exception {
        client.stop();
    }

    public void sync() throws InterruptedException {
        int expectedSize;
        int actualSize = streamHandlers.size();
        do {
            expectedSize = actualSize;
            for (HTTP2StreamHandler h : streamHandlers) {
                try {
                    // wait to receive all the response of the request
                    h.getCompletedFuture().get(h.getTimeout(), TimeUnit.MILLISECONDS);
                } catch (ExecutionException | TimeoutException e) {
                    // TODO Auto-generated catch block
                    HTTP2SampleResult sample = h.getHTTP2SampleResult();
                    // we remove the handler to avoid re checking in a potentially subsequent iteration
                    streamHandlers.remove(h);
                    expectedSize--;
                    if (e instanceof TimeoutException) {
                        sample = HTTP2SampleResult.errorResult(e, sample);
                        sample.setResponseHeaders("");
                    }
                }
            }
            actualSize = streamHandlers.size();
            /*
            since child requests might have created new handlers while iterating, we need to check if such has happened
            and re iterate (iterating over already iterated elements would produce immediate response, only penalty is
            re iteration) to wait for these new streams. Take into consideration that it can't happen a race condition
            of getting no modification in last iteration and getting a child request afterwards since child requests are
            marked as completed after adding child handlers.
             */
        } while (actualSize != expectedSize);
    }

    public List<HTTP2SampleResult> getResults() {
        return streamHandlers.stream()
                .map(HTTP2StreamHandler::getHTTP2SampleResult)
                .collect(Collectors.toList());
    }

    public void reset() {
        streamHandlers.clear();
    }

}
