package com.blazemeter.jmeter.http2.sampler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.threads.SamplePackage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class HTTP2SampleResultTest {

  private HTTP2SampleResult http2SampleResult;

  private JMeterVariables threadVars;
  private SamplePackage pack;

  @Before
  public void setup() {
    TestJMeterUtils.createJmeterEnv();
    http2SampleResult = new HTTP2SampleResult();
    threadVars = Mockito.mock(JMeterVariables.class);
    pack = Mockito.mock(SamplePackage.class);
    Mockito.when(threadVars.getObject(Mockito.any(String.class))).thenReturn(pack);
  }

  @Test
  public void setQueryStringTest() {
    http2SampleResult.setQueryString(null);
    assertEquals("", http2SampleResult.getQueryString());

    String queryString = "refURL=someRef";
    http2SampleResult.setQueryString(queryString);
    assertEquals(queryString, http2SampleResult.getQueryString());

  }

  @Test
  public void setCookiesTest() {
    http2SampleResult.setCookies(null);
    assertEquals("", http2SampleResult.getCookies());

    String cookie = "TLTSID=845E08626E1F106E011D82F69326AFDB; TLTUID=845E08626E1F106E011D82F69326AFDB";
    http2SampleResult.setCookies(cookie);

    assertEquals(cookie, http2SampleResult.getCookies());

  }

  @Test
  public void getSampleDataTest() throws MalformedURLException {
    URL url = new URL("https", "www.sprint.com", 443, "/");
    http2SampleResult.setURL(url);
    http2SampleResult.setHTTPMethod("GET");
    String cookies = "TLTSID=845E08626E1F106E011D82F69326AFDB; TLTUID=845E08626E1F106E011D82F69326AFDB";
    http2SampleResult.setCookies(cookies);
    String queryString = "refURL=someRef";
    http2SampleResult.setQueryString(queryString);
    String samplerDataRes = http2SampleResult.getSamplerData();
    String samplerDataExp =
        "GET https://www.sprint.com:443/" + "\n\n" + "GET data:" + "\n" + queryString + "\n\n"
            + "Cookie Data:" + "\n" + cookies + "\n";

    assertEquals(samplerDataExp, samplerDataRes);

  }

  @Test
  public void ColeHTTP2SampleResultTest() {
    http2SampleResult.setHTTPMethod("GET");
    String cookies = "TLTSID=845E08626E1F106E011D82F69326AFDB; TLTUID=845E08626E1F106E011D82F69326AFDB";
    http2SampleResult.setCookies(cookies);
    String queryString = "refURL=someRef";
    http2SampleResult.setQueryString(queryString);
    String redirectLocation = "www.sprint.com/apiservices/framework/initSession";
    http2SampleResult.setRedirectLocation(redirectLocation);
    HTTP2SampleResult sampleResultRes = new HTTP2SampleResult(http2SampleResult);

    assertEquals(http2SampleResult.getHTTPMethod(), sampleResultRes.getHTTPMethod());
    assertEquals(http2SampleResult.getQueryString(), sampleResultRes.getQueryString());
    assertEquals(http2SampleResult.getCookies(), sampleResultRes.getCookies());
    assertEquals(http2SampleResult.getRedirectLocation(), sampleResultRes.getRedirectLocation());
  }

  @Test
  public void testResultError() throws MalformedURLException {
    URL url = new URL("https", "www.sprint.com", 443, "/");
    HTTP2SampleResult sampleRes = new HTTP2SampleResult(url, "GET", threadVars, 0, 0, "");
    ;
    sampleRes.setRequestId("id");
    HTTP2SampleResult.setResultError(sampleRes, new Throwable());
    assertFalse(sampleRes.isPendingResponse());
    assertFalse(sampleRes.isSuccessful());
    assertEquals("id", sampleRes.getRequestId());
  }

}
