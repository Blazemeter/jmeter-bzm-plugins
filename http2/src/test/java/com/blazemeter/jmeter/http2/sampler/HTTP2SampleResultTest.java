package com.blazemeter.jmeter.http2.sampler;

import com.blazemeter.jmeter.http2.sampler.HTTP2Request;

import static org.junit.Assert.*;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.jmeter.samplers.SampleResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class HTTP2SampleResultTest {
	
	private HTTP2Connection http2ConnectionMock;
	private HTTP2Request http2Req;
	private HTTP2SampleResult http2SampleResult;
	
	@Before
    public void setup() {       
        //http2SampleResultMock = Mockito.mock(HTTP2SampleResult.class);
		http2SampleResult= new HTTP2SampleResult();
    }
	
	

	 @Test
	 public void isPendingResponseTest() throws Exception {
		 HTTP2SampleResult http2Son1, http2Son2;
		 http2Son1= new HTTP2SampleResult();
		 http2Son2= new HTTP2SampleResult();
		 http2Son2.setPendingResponse(true);
		 SampleResult[] sons= new SampleResult[2];
		 sons[0]= http2Son1;
		 sons[1]= http2Son1;
		 //http2SampleResult.addSubResult(http2Son1);
		 //http2SampleResult.addSubResult(http2Son2);
		 //Mockito.when(http2SampleResultMock.getSubResults()).thenReturn(sons);
		 //boolean result=http2SampleResult.isPendingResponse();
		 //assertEquals(true, result);
		 
		 //Mockito.whenNew(http2SampleResultMock).thenReturn();
		 //Mockito.when(http2Req.getConnection(URL, HTTP2SampleResult)).thenReturn(http2ConnectionMock);
		 
	 }

}
