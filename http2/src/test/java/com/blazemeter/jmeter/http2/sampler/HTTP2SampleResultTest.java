package com.blazemeter.jmeter.http2.sampler;



import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.jmeter.samplers.SampleResult;
import org.junit.Before;
import org.junit.Test;

public class HTTP2SampleResultTest {
	
	private HTTP2SampleResult http2SampleResult;
	
	@Before
    public void setup() {       
		http2SampleResult= new HTTP2SampleResult();
    }
	
	

	 @Test
	 public void isPendingResponseTest() throws Exception {
		 HTTP2SampleResult http2Son1, http2Son2, http2Son3;
		 http2Son1= new HTTP2SampleResult();
		 http2Son2= new HTTP2SampleResult();
		 http2Son2.setPendingResponse(true);
		 http2Son1.sampleStart();
		 http2Son2.sampleStart();
		 http2SampleResult.sampleStart();
		 SampleResult[] sons= new SampleResult[2];
		 sons[0]= http2Son1;
		 sons[1]= http2Son1;
		 
		 
		 //child that is pending is not a Secondary Request
		 http2SampleResult.addSubResult(http2Son1);
		 http2SampleResult.addSubResult(http2Son2);
		 boolean result=http2SampleResult.isPendingResponse();
		 assertFalse(result);
		 
		 //a child that is pending is a Secondary Request too
		 http2Son3= new HTTP2SampleResult();
		 http2Son3.setPendingResponse(true);
		 http2Son3.setSecondaryRequest(true);
		 http2Son3.sampleStart();
		 http2SampleResult.addSubResult(http2Son3);
		 result=http2SampleResult.isPendingResponse();
		 assertTrue(result);
		 
		 		 		 
	 }

	 
	 @Test
	 public void errorResult() throws MalformedURLException {
		 URL url = new URL("https", "www.sprint.com", 443, "/");
		 HTTP2SampleResult sampleRes =new HTTP2SampleResult(url, "GET");
		 sampleRes.setRequestId("id");
		 sampleRes = http2SampleResult.errorResult(new Throwable() , sampleRes);
		 assertEquals(false, sampleRes.isPendingResponse());
		 assertEquals(false, sampleRes.isSuccessful());
		 assertEquals("id", sampleRes.getRequestId());
		 
	 }
	 
	 @Test
	 public void setQueryStringTest(){
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
		 
		 String cookie="TLTSID=845E08626E1F106E011D82F69326AFDB; TLTUID=845E08626E1F106E011D82F69326AFDB";
		 http2SampleResult.setCookies(cookie);
		 
		 assertEquals(cookie, http2SampleResult.getCookies());
		 
	 }
	 
	 @Test
	 public void getSampleDataTest() throws MalformedURLException {
		 URL url = new URL("https", "www.sprint.com", 443, "/");
		 http2SampleResult.setURL(url);
		 http2SampleResult.setHTTPMethod("GET");
		 String cookies="TLTSID=845E08626E1F106E011D82F69326AFDB; TLTUID=845E08626E1F106E011D82F69326AFDB";
		 http2SampleResult.setCookies(cookies);
		 String queryString = "refURL=someRef";
		 http2SampleResult.setQueryString(queryString);
		 String samplerDataRes= http2SampleResult.getSamplerData();
		 String samplerDataExp = "GET https://www.sprint.com:443/" + "\n\n" + "GET data:" + "\n" + queryString + "\n\n" + "Cookie Data:" + "\n" + cookies + "\n";
		 
		 assertEquals(samplerDataExp, samplerDataRes);
		 
	 }
	 
	 @Test
	 public void ColeHTTP2SampleResultTest(){
		 http2SampleResult.setHTTPMethod("GET");
		 String cookies="TLTSID=845E08626E1F106E011D82F69326AFDB; TLTUID=845E08626E1F106E011D82F69326AFDB";
		 http2SampleResult.setCookies(cookies);
		 String queryString = "refURL=someRef";
		 http2SampleResult.setQueryString(queryString);
		 String redirectLocation = "www.sprint.com/apiservices/framework/initSession";
		 http2SampleResult.setRedirectLocation(redirectLocation);
		 HTTP2SampleResult sampleResultRes = new HTTP2SampleResult(http2SampleResult);
		 
		 assertEquals(http2SampleResult.getHTTPMethod(), sampleResultRes.getHTTPMethod() );
		 assertEquals(http2SampleResult.getQueryString(), sampleResultRes.getQueryString() );
		 assertEquals(http2SampleResult.getCookies(), sampleResultRes.getCookies() );
		 assertEquals(http2SampleResult.getRedirectLocation(), sampleResultRes.getRedirectLocation() );
	 }
	 
}
