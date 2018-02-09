package com.blazemeter.jmeter.http2.sampler;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.protocol.http.util.HTTPFileArgs;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URL;

import static org.junit.Assert.*;

public class HTTP2RequestTest {

    private HTTP2Connection http2ConnectionMock;
    private HTTP2Request http2Req;

    @Before
    public void setup() {
        http2ConnectionMock = Mockito.mock(HTTP2Connection.class);
        http2Req = new HTTP2Request();
        http2Req.setThreadName("10");
        http2Req.setProperty(HTTP2Request.DOMAIN, "www.sprint.com");
    }

    @After
    public void teardown() {
        http2Req.threadFinished();
    }

    @Test
    public void sampleTest1() throws Exception {
        URL url = new URL(http2Req.getProtocol(), "www.sprint.com", 443, "/");

        HTTP2Connection connection = Mockito.mock(HTTP2Connection.class);

        Mockito.when(connection.isClosed()).thenReturn(true);
        Mockito.doNothing().when(connection).connect(Mockito.any(String.class), Mockito.any(Integer.class));
        Mockito.when(connection.getConnectionId()).thenReturn("www.sprint.com:443");

        http2Req.setProperty(HTTP2Request.METHOD, "GET");
        HTTP2SampleResult sampleResult = new HTTP2SampleResult(url, http2Req.getMethod());
        http2Req.addConnection("10www.sprint.com443", connection);
        http2Req.setConnection(url, sampleResult);
        http2Req.setProperty(new BooleanProperty(HTTP2Request.SYNC_REQUEST, true));
        http2Req.sample(url, "GET", http2ConnectionMock, sampleResult);
    }

    @Test
    public void sampleTest2() throws Exception {
        Arguments args = new Arguments();
        String text = "{\"header\":{\"applicationId\":\"HJS\"},\"initSession\":{}}";
        HTTPArgument arg = new HTTPArgument("", text.replaceAll("\n", "\r\n"), false);
        arg.setAlwaysEncoded(false);
        args.addArgument(arg);
        http2Req.setProperty(new TestElementProperty(HTTP2Request.ARGUMENTS, args));

        URL url = new URL("https", "www.sprint.com", 443, "/apiservices/framework/initSession");

        HTTP2Connection connection = Mockito.mock(HTTP2Connection.class);

        Mockito.when(connection.isClosed()).thenReturn(true);
        Mockito.doNothing().when(connection).connect(Mockito.any(String.class), Mockito.any(Integer.class));
        Mockito.when(connection.getConnectionId()).thenReturn("www.sprint.com:443");

        HTTP2SampleResult sampleResult = new HTTP2SampleResult(url, "POST");
        http2Req.addConnection("10www.sprint.com443", connection);
        http2Req.setConnection(url, sampleResult);
        http2Req.setProperty(new BooleanProperty(HTTP2Request.SYNC_REQUEST, true));
        http2Req.sample(url, "POST", http2Req.getConnection(), sampleResult);
    }

    @Test
    public void sampleTest3() throws Exception {
        URL url = new URL(http2Req.getProtocol(), "www.sprint.com", 443, "/");
        HTTP2SampleResult sampleRes = new HTTP2SampleResult(url, "GET");

        // connection is null so sample fails
        sampleRes = http2Req.sample(url, "GET", null, sampleRes);
        assertEquals(false, sampleRes.isPendingResponse());
        assertEquals(false, sampleRes.isSuccessful());
    }

    @Test
    public void getUrlTest() throws Exception {
        http2Req.setProperty(HTTP2Request.PATH, "/shop/device/list/PHONE");

        URL urlRes = http2Req.getUrl();
        URL urlExp = new URL("https", "www.sprint.com", 443, "/shop/device/list/PHONE");

        assertEquals(urlExp, urlRes);

        http2Req.setProperty(HTTP2Request.PATH, "https://www.sprint.com/shop/device/list/PHONE");
        urlRes = http2Req.getUrl();
        assertEquals(urlExp, urlRes);

        http2Req.setProperty(HTTP2Request.PATH, "shop/device/list/PHONE");
        urlRes = http2Req.getUrl();
        assertEquals(urlExp, urlRes);

        http2Req.setProperty(HTTP2Request.PORT, "8080");
        urlRes = http2Req.getUrl();
        urlExp = new URL("https", "www.sprint.com", 8080, "/shop/device/list/PHONE");
        assertEquals(urlExp, urlRes);
    }

    @Test
    public void getPortTest() {
        http2Req.setProtocol(HTTPConstants.PROTOCOL_HTTPS);
        int portExp = 443;
        int portRes = http2Req.getPort();
        assertEquals(portExp, portRes);

        http2Req.setProtocol(HTTPConstants.PROTOCOL_HTTP);
        portExp = 80;
        portRes = http2Req.getPort();
        assertEquals(portExp, portRes);

    }

    @Test
    public void setManagersTest() {
        HeaderManager hManExp = new HeaderManager();
        http2Req.addTestElement(hManExp);
        HeaderManager hManRes = http2Req.getHeaderManager();
        assertEquals(hManExp, hManRes);

        CookieManager cManExp = new CookieManager();
        http2Req.addTestElement(cManExp);
        CookieManager cManRes = http2Req.getCookieManager();
        assertEquals(cManExp, cManRes);

        CacheManager cachManExp = new CacheManager();
        http2Req.addTestElement(cachManExp);
        CacheManager cachManRes = http2Req.getCacheManager();
        assertEquals(cachManExp, cachManRes);
    }

    @Test
    public void createPostContentTest() {
        String text = "{\"header\":{\"applicationId\":\"HJS\"},\"initSession\":{}}";

        DataPostContent dataPostExp = new DataPostContent();
        dataPostExp.setDataPath("/apiservices/framework/initSession");
        dataPostExp.setPayload(text.getBytes());

        Arguments args = new Arguments();

        HTTPArgument arg = new HTTPArgument("", text.replaceAll("\n", "\r\n"), false);
        arg.setAlwaysEncoded(false);
        args.addArgument(arg);
        http2Req.setProperty(new TestElementProperty(HTTP2Request.ARGUMENTS, args));
        http2Req.setProperty(HTTP2Request.PATH, "/apiservices/framework/initSession");
        DataPostContent dataPostRes = http2Req.createPostContent("POST");

        assertEquals(dataPostExp.getDataPath(), dataPostRes.getDataPath());
    }

    @Test
    public void getHTTPFilesTest() {
        HTTPFileArg[] fileArgsRes = http2Req.getHTTPFiles();
        assertNotEquals(null, fileArgsRes);
        HTTPFileArgs fileArgs = new HTTPFileArgs();
        HTTPFileArg[] fileArgsExp = fileArgs.asArray();
        http2Req.setProperty(new TestElementProperty("HTTPsampler.Files", fileArgs));
        fileArgsRes = http2Req.getHTTPFiles();

        assertArrayEquals(fileArgsExp, fileArgsRes);
    }

    @Test
    public void getSendFileAsPostBodyTest() {
        HTTPFileArgs fileArgs = new HTTPFileArgs();
        http2Req.setProperty(new TestElementProperty("HTTPsampler.Files", fileArgs));
        boolean valueRes = http2Req.getSendFileAsPostBody();
        assertFalse(valueRes);
    }

    @Test
    public void sampleMainTest() {
        http2Req.sample();
    }

}
