package com.blazemeter.jmeter.http2.sampler;

import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.protocol.http.util.HTTPFileArgs;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.threads.SamplePackage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import kg.apc.emulators.TestJMeterUtils;
import java.net.URL;

import static org.junit.Assert.*;

public class HTTP2RequestTest {

    private HTTP2Request http2Req;


    @Before
    public void setup() {
    	TestJMeterUtils.createJmeterEnv();
        http2Req = new HTTP2Request();
        http2Req.setThreadName("10");
        http2Req.setProperty(HTTP2Request.DOMAIN, "www.sprint.com");
    }

    @After
    public void teardown() {
        http2Req.threadFinished();
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
}
