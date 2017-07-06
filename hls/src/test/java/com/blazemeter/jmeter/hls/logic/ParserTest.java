package com.blazemeter.jmeter.hls.logic;

import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.http.protocol.HTTP.USER_AGENT;
import static org.junit.Assert.assertEquals;

public class ParserTest {
	
	private URL urlMock;
	private HttpURLConnection httpURLConnectionMock;
	private SampleResult sampleResultMock;
	private Parser p;
	
	@Before
    public void setup() {
        urlMock = Mockito.mock(URL.class);
		httpURLConnectionMock = Mockito.mock(HttpURLConnection.class);
		sampleResultMock = Mockito.mock(SampleResult.class);
		p = new Parser();
    }
	
	
	@Test
    public void testGetBaseUrl() throws Exception{
		
		
		Mockito.when(urlMock.openConnection()).thenReturn(httpURLConnectionMock);
		Mockito.when(urlMock.toString()).thenReturn("http://www.mock.com");
		
		String payload = "line1\nline2\nline3\nline4\n";
		InputStream stream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
		
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
        List<String> header1 = new ArrayList<String>();
        List<String> header2 = new ArrayList<String>();
        List<String> header3 = new ArrayList<String>();

        header1.add("header11");
        header1.add("header12");
        header1.add("header13");
        
        header2.add("header21");
        header2.add("header22");
        header2.add("header23");
        
        header3.add("header31");
        
        headers.put("headerKey1", header1);
        headers.put("headerKey2", header2);
        headers.put("headerKey3", header3);
		
		Mockito.when(httpURLConnectionMock.getResponseCode()).thenReturn(200);
		Mockito.when(httpURLConnectionMock.getInputStream()).thenReturn(stream);
		Mockito.when(httpURLConnectionMock.getRequestMethod()).thenReturn("GET");
		Mockito.when(httpURLConnectionMock.getHeaderFields()).thenReturn(headers);
		Mockito.when(httpURLConnectionMock.getResponseMessage()).thenReturn("OK");
		Mockito.when(httpURLConnectionMock.getContentType()).thenReturn("application/json;charset=UTF-8");
		
		DataRequest dataRequestResult = p.getBaseUrl(urlMock, sampleResultMock, true);

		DataRequest dataRequestExpected = new DataRequest();  
		
		dataRequestExpected.setRequestHeaders("GET  http://www.mock.com\n");
		dataRequestExpected.setHeaders(headers);
		dataRequestExpected.setResponse(payload);
		dataRequestExpected.setResponseCode("200");
		dataRequestExpected.setResponseMessage("OK");
		dataRequestExpected.setContentType("application/json;charset=UTF-8");
		dataRequestExpected.setSuccess(true);
		dataRequestExpected.setSentBytes(payload.length());
		dataRequestExpected.setContentEncoding("UTF-8");
		
		assertEquals(dataRequestExpected, dataRequestResult);
        
    }	
	
	@Test
    public void testExtractUriMaster() throws Exception{
		
//		
//		p.extractUriMaster(String res, String resolution, String bandwidth, String bandSelected,
//				String resolSelected, String urlVideoType)
//		
//		Mockito.when(urlMock.openConnection()).thenReturn(httpURLConnectionMock);
//		Mockito.when(urlMock.toString()).thenReturn("http://www.mock.com");
//		
//		String payload = "line1\nline2\nline3\nline4\n";
//		InputStream stream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
//		
//		Map<String, List<String>> headers = new HashMap<String, List<String>>();
//        List<String> header1 = new ArrayList<String>();
//        List<String> header2 = new ArrayList<String>();
//        List<String> header3 = new ArrayList<String>();
//
//        header1.add("header11");
//        header1.add("header12");
//        header1.add("header13");
//        
//        header2.add("header21");
//        header2.add("header22");
//        header2.add("header23");
//        
//        header3.add("header31");
//        
//        headers.put("headerKey1", header1);
//        headers.put("headerKey2", header2);
//        headers.put("headerKey3", header3);
//		
//		Mockito.when(httpURLConnectionMock.getResponseCode()).thenReturn(200);
//		Mockito.when(httpURLConnectionMock.getInputStream()).thenReturn(stream);
//		Mockito.when(httpURLConnectionMock.getRequestMethod()).thenReturn("GET");
//		Mockito.when(httpURLConnectionMock.getHeaderFields()).thenReturn(headers);
//		Mockito.when(httpURLConnectionMock.getResponseMessage()).thenReturn("OK");
//		Mockito.when(httpURLConnectionMock.getContentType()).thenReturn("application/json;charset=UTF-8");
//		
//		
//		
//		
//		Parser p = new Parser();
//		DataRequest dataRequestResult = p.getBaseUrl(urlMock, sampleResultMock, true);
//
//		DataRequest dataRequestExpected = new DataRequest();  
//		
//		dataRequestExpected.setRequestHeaders("GET  http://www.mock.com\n");
//		dataRequestExpected.setHeaders(headers);
//		dataRequestExpected.setResponse(payload);
//		dataRequestExpected.setResponseCode("200");
//		dataRequestExpected.setResponseMessage("OK");
//		dataRequestExpected.setContentType("application/json;charset=UTF-8");
//		dataRequestExpected.setSuccess(true);
//		dataRequestExpected.setSentBytes(payload.length());
//		dataRequestExpected.setContentEncoding("UTF-8");
//		
//		assertEquals(dataRequestExpected, dataRequestResult);
        
    }	
}
