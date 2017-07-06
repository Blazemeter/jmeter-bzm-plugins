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
		
		String res = "#EXTM3U\n#EXT-X-VERSION:4\n#EXT-X-STREAM-INF:AUDIO=\"600k\",BANDWIDTH=1395723,PROGRAM-ID=1,CODECS=\"avc1.42c01e,mp4a.40.2\",RESOLUTION=640x360,SUBTITLES=\"subs\"\n/videos/DianaLaufenberg_2010X/video/600k.m3u8?preroll=Thousands&uniqueId=4df94b1d\n#EXT-X-STREAM-INF:AUDIO=\"600k\",BANDWIDTH=170129,PROGRAM-ID=1,CODECS=\"avc1.42c00c,mp4a.40.2\",RESOLUTION=320x180,SUBTITLES=\"subs\"\n/videos/DianaLaufenberg_2010X/video/64k.m3u8?preroll=Thousands&uniqueId=4df94b1d\n#EXT-X-STREAM-INF:AUDIO=\"600k\",BANDWIDTH=425858,PROGRAM-ID=1,CODECS=\"avc1.42c015,mp4a.40.2\",RESOLUTION=512x288,SUBTITLES=\"subs\"\n/videos/DianaLaufenberg_2010X/video/180k.m3u8?preroll=Thousands&uniqueId=4df94b1d\n#EXT-X-STREAM-INF:AUDIO=\"600k\",BANDWIDTH=718158,PROGRAM-ID=1,CODECS=\"avc1.42c015,mp4a.40.2\",RESOLUTION=512x288,SUBTITLES=\"subs\"\n/videos/DianaLaufenberg_2010X/video/320k.m3u8?preroll=Thousands&uniqueId=4df94b1d";
		
/*
#EXTM3U
#EXT-X-VERSION:4
#EXT-X-STREAM-INF:AUDIO="600k",BANDWIDTH=1395723,PROGRAM-ID=1,CODECS="avc1.42c01e,mp4a.40.2",RESOLUTION=640x360,SUBTITLES="subs"
/videos/DianaLaufenberg_2010X/video/600k.m3u8?preroll=Thousands&uniqueId=4df94b1d
#EXT-X-STREAM-INF:AUDIO="600k",BANDWIDTH=170129,PROGRAM-ID=1,CODECS="avc1.42c00c,mp4a.40.2",RESOLUTION=320x180,SUBTITLES="subs"
/videos/DianaLaufenberg_2010X/video/64k.m3u8?preroll=Thousands&uniqueId=4df94b1d
#EXT-X-STREAM-INF:AUDIO="600k",BANDWIDTH=425858,PROGRAM-ID=1,CODECS="avc1.42c015,mp4a.40.2",RESOLUTION=512x288,SUBTITLES="subs"
/videos/DianaLaufenberg_2010X/video/180k.m3u8?preroll=Thousands&uniqueId=4df94b1d
#EXT-X-STREAM-INF:AUDIO="600k",BANDWIDTH=718158,PROGRAM-ID=1,CODECS="avc1.42c015,mp4a.40.2",RESOLUTION=512x288,SUBTITLES="subs"
/videos/DianaLaufenberg_2010X/video/320k.m3u8?preroll=Thousands&uniqueId=4df94b1d
 */
		
		
		String result = p.extractUriMaster(res, "640x360", "1395723", "customBandwidth", "customResolution", "Bandwidth");
		String expected = "/videos/DianaLaufenberg_2010X/video/600k.m3u8?preroll=Thousands&uniqueId=4df94b1d";
		
		System.out.println(res);
		System.out.println("***********************");
		
		System.out.println(result);
		System.out.println(expected);
		System.out.println("*********");
//		assertEquals(expected, result);
		
		result = p.extractUriMaster(res, "640x360", "", "minBandwidth", "customResolution", "Bandwidth");
		expected = "/videos/DianaLaufenberg_2010X/video/64k.m3u8?preroll=Thousands&uniqueId=4df94b1d";
		
		System.out.println(result);
		System.out.println(expected);
		System.out.println("*********");
//		assertEquals(expected, result);
		
		result = p.extractUriMaster(res, "640x360", "", "maxBandwidth", "customResolution", "Bandwidth");
		expected = "/videos/DianaLaufenberg_2010X/video/600k.m3u8?preroll=Thousands&uniqueId=4df94b1d";
		
		System.out.println(result);
		System.out.println(expected);
		System.out.println("*********");
//		assertEquals(expected, result);
        
		result = p.extractUriMaster(res, "640x360", "1395723", "customBandwidth", "customResolution", "");
		expected = "/videos/DianaLaufenberg_2010X/video/600k.m3u8?preroll=Thousands&uniqueId=4df94b1d";
		
		System.out.println(result);
		System.out.println(expected);
		System.out.println("*********");
//		assertEquals(expected, result);
		
		result = p.extractUriMaster(res, "640x360", "", "minBandwidth", "customResolution", "");
		expected = "/videos/DianaLaufenberg_2010X/video/600k.m3u8?preroll=Thousands&uniqueId=4df94b1d";
		
		System.out.println(result);
		System.out.println(expected);
		System.out.println("*********");
//		assertEquals(expected, result);
		
		result = p.extractUriMaster(res, "640x360", "", "maxBandwidth", "customResolution", "");
		expected = "/videos/DianaLaufenberg_2010X/video/600k.m3u8?preroll=Thousands&uniqueId=4df94b1d";
		
		System.out.println(result);
		System.out.println(expected);
		System.out.println("*********");
//		assertEquals(expected, result);
		
		result = p.extractUriMaster(res, "", "1395723", "customBandwidth", "maxResolution", "");
		expected = "/videos/DianaLaufenberg_2010X/video/600k.m3u8?preroll=Thousands&uniqueId=4df94b1d";
		
		System.out.println(result);
		System.out.println(expected);
		System.out.println("*********");
//		assertEquals(expected, result);
		
		result = p.extractUriMaster(res, "", "", "minBandwidth", "maxResolution", "");
		expected = "/videos/DianaLaufenberg_2010X/video/600k.m3u8?preroll=Thousands&uniqueId=4df94b1d";
		
		System.out.println(result);
		System.out.println(expected);
		System.out.println("*********");
//		assertEquals(expected, result);
		
		result = p.extractUriMaster(res, "", "", "maxBandwidth", "maxResolution", "");
		expected = "/videos/DianaLaufenberg_2010X/video/600k.m3u8?preroll=Thousands&uniqueId=4df94b1d";
		
		System.out.println(result);
		System.out.println(expected);
		System.out.println("*********");
//		assertEquals(expected, result);
		
		result = p.extractUriMaster(res, "", "1395723", "customBandwidth", "minResolution", "");
		expected = "/videos/DianaLaufenberg_2010X/video/64k.m3u8?preroll=Thousands&uniqueId=4df94b1d";
		
		System.out.println(result);
		System.out.println(expected);
		System.out.println("*********");
//		assertEquals(expected, result);
		
		result = p.extractUriMaster(res, "", "", "minBandwidth", "minResolution", "");
		expected = "/videos/DianaLaufenberg_2010X/video/64k.m3u8?preroll=Thousands&uniqueId=4df94b1d";
		
		System.out.println(result);
		System.out.println(expected);
		System.out.println("*********");
//		assertEquals(expected, result);
		
		result = p.extractUriMaster(res, "", "", "maxBandwidth", "minResolution", "");
		expected = "/videos/DianaLaufenberg_2010X/video/64k.m3u8?preroll=Thousands&uniqueId=4df94b1d";
		
		System.out.println(result);
		System.out.println(expected);
		System.out.println("*********");
//		assertEquals(expected, result);
		
    }	
}
