package com.blazemeter.jmeter.http2.sampler;

import com.blazemeter.jmeter.http2.sampler.HTTP2Request;

import static org.junit.Assert.*;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.jmeter.samplers.SampleResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class HTTP2RequestTest {
	
	private HTTP2Connection http2ConnectionMock;
	private HTTP2Request http2Req;
	private HTTP2SampleResult http2SampleResultMock;
	
	@Before
    public void setup() {
        http2ConnectionMock = Mockito.mock(HTTP2Connection.class);
        http2SampleResultMock = Mockito.mock(HTTP2SampleResult.class);
        http2Req = new HTTP2Request();
    }
	
	

	 @Test
	 public void sampleTest() throws Exception {
		 
		 //Mockito.when(http2SampleResultMock).thenReturn();
		 //Mockito.when(http2Req.getConnection(URL, HTTP2SampleResult)).thenReturn(http2ConnectionMock);
		 
	 }

}
