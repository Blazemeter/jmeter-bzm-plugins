package com.blazemeter.jmeter.hls.logic;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.samplers.SampleResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


public class HlsSamplerTest {
	
	

	private HlsSampler sampler;
	private Parser parserMock;
	
	@Before
	public void setup()
	        throws Exception {
//        TestJMeterUtils.createJmeterEnv();
		
        parserMock = Mockito.mock(Parser.class);
        sampler = new HlsSampler();
		sampler.setURLData("http://www.mock.com/path");
		sampler.setResData("640x360");
		sampler.setNetworkData("1395723");
		sampler.setBandwidthType("customBandwidth");
		sampler.setResolutionType("customResolution");
		sampler.setUrlVideoType("Bandwidth");
		sampler.setPRotocol("https");
		sampler.setPlaySecondsData("20");
		sampler.setVideoDuration(true);
		sampler.setParser(parserMock);
		sampler.setName("Test");
	}
	
	@Test
	public void testSample() throws Exception {

		DataRequest respond1 = new DataRequest();
		DataRequest respond2 = new DataRequest();
		DataRequest respond3 = new DataRequest();
		DataRequest respond4 = new DataRequest();
		DataRequest respond5 = new DataRequest();
		DataFragment f1 = new DataFragment("10", "https://pb.tedcdn.com/bumpers/hls/video/in/Thousands-320k_1.ts");
		DataFragment f2 = new DataFragment("10", "https://pb.tedcdn.com/bumpers/hls/video/in/Thousands-320k_2.ts");
		DataFragment f3 = new DataFragment("10", "https://pb.tedcdn.com/bumpers/hls/video/in/Thousands-320k_3.ts");
		List<DataFragment> fragments = new ArrayList<DataFragment>();
		fragments.add(f1);
		fragments.add(f2);
		fragments.add(f3);

		String payload1 = "#EXTM3U\n#EXT-X-VERSION:4\n#EXT-X-STREAM-INF:AUDIO=\"600k\",BANDWIDTH=1395723,PROGRAM-ID=1,CODECS=\"avc1.42c01e,mp4a.40.2\",RESOLUTION=640x360,SUBTITLES=\"subs\"\n/videos/DianaLaufenberg_2010X/video/600k.m3u8?preroll=Thousands&uniqueId=4df94b1d\n#EXT-X-STREAM-INF:AUDIO=\"600k\",BANDWIDTH=170129,PROGRAM-ID=1,CODECS=\"avc1.42c00c,mp4a.40.2\",RESOLUTION=320x180,SUBTITLES=\"subs\"\n/videos/DianaLaufenberg_2010X/video/64k.m3u8?preroll=Thousands&uniqueId=4df94b1d\n#EXT-X-STREAM-INF:AUDIO=\"600k\",BANDWIDTH=425858,PROGRAM-ID=1,CODECS=\"avc1.42c015,mp4a.40.2\",RESOLUTION=512x288,SUBTITLES=\"subs\"\n/videos/DianaLaufenberg_2010X/video/180k.m3u8?preroll=Thousands&uniqueId=4df94b1d\n#EXT-X-STREAM-INF:AUDIO=\"600k\",BANDWIDTH=718158,PROGRAM-ID=1,CODECS=\"avc1.42c015,mp4a.40.2\",RESOLUTION=512x288,SUBTITLES=\"subs\"\n/videos/DianaLaufenberg_2010X/video/320k.m3u8?preroll=Thousands&uniqueId=4df94b1d";
		String payload2 = "#EXTM3U\n#EXT-X-TARGETDURATION:10\n#EXT-X-VERSION:4\n#EXT-X-MEDIA-SEQUENCE:0\n#EXT-X-PLAYLIST-TYPE:VOD\n#EXTINF:5.0000,\n#EXT-X-BYTERANGE:440672@0\nhttps://pb.tedcdn.com/bumpers/hls/video/in/Thousands-320k.ts\n#EXTINF:5.0000,\n#EXT-X-BYTERANGE:94000@440672\nhttps://pb.tedcdn.com/bumpers/hls/video/in/Thousands-320k.ts\n#EXTINF:1.9583,\n#EXT-X-BYTERANGE:22748@534672\nhttps://pb.tedcdn.com/bumpers/hls/video/in/Thousands-320k.ts\n#EXT-X-DISCONTINUITY";
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

		respond1.setRequestHeaders("GET  http://www.mock.com/path\n");
		respond1.setHeaders(headers);
		respond1.setResponse(payload1);
		respond1.setResponseCode("200");
		respond1.setResponseMessage("OK");
		respond1.setContentType("application/json;charset=UTF-8");
		respond1.setSuccess(true);
		respond1.setSentBytes(payload1.length());
		respond1.setContentEncoding("UTF-8");

		respond2.setRequestHeaders("GET  http://www.mock.com/path/videos/DianaLaufenberg_2010X/video/600k.m3u8?preroll=Thousands&uniqueId=4df94b1d\n");
		respond2.setHeaders(headers);
		respond2.setResponse(payload2);
		respond2.setResponseCode("200");
		respond2.setResponseMessage("OK");
		respond2.setContentType("application/json;charset=UTF-8");
		respond2.setSuccess(true);
		respond2.setSentBytes(payload2.length());
		respond2.setContentEncoding("UTF-8");

		respond3.setRequestHeaders("GET  https://pb.tedcdn.com/bumpers/hls/video/in/Thousands-320k_1.ts\n");
		respond3.setHeaders(headers);
		respond3.setResponse("chunck");
		respond3.setResponseCode("200");
		respond3.setResponseMessage("OK");
		respond3.setContentType("application/json;charset=UTF-8");
		respond3.setSuccess(true);
		respond3.setSentBytes("chunck".length());
		respond3.setContentEncoding("UTF-8");

		respond4.setRequestHeaders("GET  https://pb.tedcdn.com/bumpers/hls/video/in/Thousands-320k_2.ts\n");
		respond4.setHeaders(headers);
		respond4.setResponse("chunck");
		respond4.setResponseCode("200");
		respond4.setResponseMessage("OK");
		respond4.setContentType("application/json;charset=UTF-8");
		respond4.setSuccess(true);
		respond4.setSentBytes("chunck".length());
		respond4.setContentEncoding("UTF-8");

		respond5.setRequestHeaders("GET  https://pb.tedcdn.com/bumpers/hls/video/in/Thousands-320k_3.ts\n");
		respond5.setHeaders(headers);
		respond5.setResponse("chunck");
		respond5.setResponseCode("200");
		respond5.setResponseMessage("OK");
		respond5.setContentType("application/json;charset=UTF-8");
		respond5.setSuccess(true);
		respond5.setSentBytes("chunck".length());
		respond5.setContentEncoding("UTF-8");

		Mockito.when(parserMock.getBaseUrl(Mockito.any(URL.class),Mockito.any(SampleResult.class),Mockito.anyBoolean()))
			.thenReturn(respond1)
			.thenReturn(respond2)
			.thenReturn(respond3)
			.thenReturn(respond4)
			.thenReturn(respond5);

		Mockito.when(parserMock.extractMediaUrl(Mockito.any(String.class),Mockito.any(String.class),Mockito.any(String.class),Mockito.any(String.class),Mockito.any(String.class)))
			.thenReturn("/videos/DianaLaufenberg_2010X/video/600k.m3u8?preroll=Thousands&uniqueId=4df94b1d");
		Mockito.when(parserMock.extractVideoUrl(Mockito.any()))
				.thenReturn(fragments);
		Mockito.when(parserMock.isLive(Mockito.any(String.class)))
				.thenReturn(false);


		SampleResult result = sampler.sample(null);

		assertEquals("GET  http://www.mock.com/path\n\n\n\n\n", result.getRequestHeaders());
		assertEquals(true, result.isSuccessful());
		assertEquals("OK", result.getResponseMessage());
		assertEquals("Test", result.getSampleLabel());
		assertEquals("headerKey1 : header11 header12 header13\nheaderKey2 : header21 header22 header23\nheaderKey3 : header31\n", result.getResponseHeaders());
		assertEquals(new String(payload1.getBytes(), "UTF-8"), new String(result.getResponseData(), "UTF-8"));
		assertEquals("200", result.getResponseCode());
		assertEquals("application/json;charset=UTF-8", result.getContentType());
		assertEquals("UTF-8", result.getDataEncodingNoDefault());

		SampleResult[] subresults  = result.getSubResults();
		assertEquals("GET  http://www.mock.com/path/videos/DianaLaufenberg_2010X/video/600k.m3u8?preroll=Thousands&uniqueId=4df94b1d\n\n\n\n\n", subresults[0].getRequestHeaders());
		assertEquals(true, subresults[0].isSuccessful());
		assertEquals("OK", subresults[0].getResponseMessage());
		assertEquals("600k.m3u8?preroll=Thousands&uniqueId=4df94b1d", subresults[0].getSampleLabel());
		assertEquals("headerKey1 : header11 header12 header13\nheaderKey2 : header21 header22 header23\nheaderKey3 : header31\n", subresults[0].getResponseHeaders());
		assertEquals(new String(payload2.getBytes(), "UTF-8"), new String(subresults[0].getResponseData(), "UTF-8"));
		assertEquals("200", subresults[0].getResponseCode());
		assertEquals("application/json;charset=UTF-8", subresults[0].getContentType());
		assertEquals("UTF-8", subresults[0].getDataEncodingNoDefault());

		SampleResult[] subsubresults  = subresults[0].getSubResults();

		assertEquals("GET  https://pb.tedcdn.com/bumpers/hls/video/in/Thousands-320k_1.ts\n\n\n\n\n", subsubresults[0].getRequestHeaders());
		assertEquals(true, subsubresults[0].isSuccessful());
		assertEquals("OK", subsubresults[0].getResponseMessage());
		assertEquals("Thousands-320k_1.ts", subsubresults[0].getSampleLabel());
		assertEquals("URL: https://pb.tedcdn.com/bumpers/hls/video/in/Thousands-320k_1.ts\nheaderKey1 : header11 header12 header13\nheaderKey2 : header21 header22 header23\nheaderKey3 : header31\n", subsubresults[0].getResponseHeaders());
		assertEquals("200", subsubresults[0].getResponseCode());
		assertEquals("application/json;charset=UTF-8", subsubresults[0].getContentType());
		assertEquals("UTF-8", subsubresults[0].getDataEncodingNoDefault());

		assertEquals("GET  https://pb.tedcdn.com/bumpers/hls/video/in/Thousands-320k_2.ts\n\n\n\n\n", subsubresults[1].getRequestHeaders());
		assertEquals(true, subsubresults[1].isSuccessful());
		assertEquals("OK", subsubresults[1].getResponseMessage());
		assertEquals("Thousands-320k_2.ts", subsubresults[1].getSampleLabel());
		assertEquals("URL: https://pb.tedcdn.com/bumpers/hls/video/in/Thousands-320k_2.ts\nheaderKey1 : header11 header12 header13\nheaderKey2 : header21 header22 header23\nheaderKey3 : header31\n", subsubresults[1].getResponseHeaders());
		assertEquals("200", subsubresults[1].getResponseCode());
		assertEquals("application/json;charset=UTF-8", subsubresults[1].getContentType());
		assertEquals("UTF-8", subsubresults[1].getDataEncodingNoDefault());

		assertEquals("GET  https://pb.tedcdn.com/bumpers/hls/video/in/Thousands-320k_3.ts\n\n\n\n\n", subsubresults[2].getRequestHeaders());
		assertEquals(true, subsubresults[2].isSuccessful());
		assertEquals("OK", subsubresults[2].getResponseMessage());
		assertEquals("Thousands-320k_3.ts", subsubresults[2].getSampleLabel());
		assertEquals("URL: https://pb.tedcdn.com/bumpers/hls/video/in/Thousands-320k_3.ts\nheaderKey1 : header11 header12 header13\nheaderKey2 : header21 header22 header23\nheaderKey3 : header31\n", subsubresults[2].getResponseHeaders());
		assertEquals("200", subsubresults[2].getResponseCode());
		assertEquals("application/json;charset=UTF-8", subsubresults[2].getContentType());
		assertEquals("UTF-8", subsubresults[2].getDataEncodingNoDefault());








	}

}
