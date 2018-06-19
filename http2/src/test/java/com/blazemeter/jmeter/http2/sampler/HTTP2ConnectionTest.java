package com.blazemeter.jmeter.http2.sampler;

import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.eclipse.jetty.http2.api.Session;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import kg.apc.emulators.TestJMeterUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class HTTP2ConnectionTest {

    private HTTP2Connection http2Connection;
    private HeaderManager headerManagerMock;
    private CookieManager cookieManagerMock;
    private URL urlMock;
    private Session sessionMock;

    @Before
    public void setup() throws Exception {
    	TestJMeterUtils.createJmeterEnv();
        headerManagerMock = Mockito.mock(HeaderManager.class);
        urlMock = Mockito.mock(URL.class);
        cookieManagerMock = Mockito.mock(CookieManager.class);
        sessionMock = Mockito.mock(Session.class);
        http2Connection = new HTTP2Connection("1", true);
    }

    @Test
    public void sendTest() throws Exception {

        CollectionProperty collProp = new CollectionProperty("HeaderManager.headers", new ArrayList<>());
        Header header1 = new Header("accept", "application/json, text/plain, */*");
        Header header2 = new Header("content-type", "application/json;charset=UTF-8");
        collProp.addItem(header1);
        collProp.addItem(header2);

        Mockito.when(headerManagerMock.getHeaders()).thenReturn(collProp);
        Mockito.when(cookieManagerMock.getCookieHeaderForURL(urlMock)).thenReturn("TLTSID=F1F77E38627810620014CC0EAD1EEEB4; Path=/; Domain=.sprint.com");
        Mockito.when(urlMock.toString()).thenReturn("https://www.spring.com");

        HTTP2SampleResult sampleResult1 = new HTTP2SampleResult();

        http2Connection.setSession(sessionMock);
        http2Connection.send("GET", urlMock, headerManagerMock, cookieManagerMock, null, sampleResult1, 0);
    }

    @Test
    public void addPendingResponsesTest() throws InterruptedException {
        HTTP2SampleResult pendingResponse = new HTTP2SampleResult();
        pendingResponse.setPendingResponse(false);
        HTTP2StreamHandler streamHandler = new HTTP2StreamHandler(http2Connection, headerManagerMock, cookieManagerMock, pendingResponse);
        http2Connection.addStreamHandler(streamHandler);
        assertEquals(Collections.singletonList(pendingResponse), http2Connection.awaitResponses());
    }

}
