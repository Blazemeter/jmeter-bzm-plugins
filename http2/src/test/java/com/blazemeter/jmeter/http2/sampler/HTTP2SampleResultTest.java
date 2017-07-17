package com.blazemeter.jmeter.http2.sampler;

import com.blazemeter.jmeter.http2.sampler.HTTP2Request;

import static org.junit.Assert.*;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.jmeter.samplers.SampleResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

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
}
