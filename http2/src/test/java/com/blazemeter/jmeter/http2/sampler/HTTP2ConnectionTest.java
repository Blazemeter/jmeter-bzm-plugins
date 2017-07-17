package com.blazemeter.jmeter.http2.sampler;


import static org.junit.Assert.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.eclipse.jetty.http2.api.Session;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.CookieManager;

public class HTTP2ConnectionTest {
	
	private HTTP2Connection http2Connection;
	private HeaderManager headerManagerMock;
	private CookieManager cookieManagerMock;
	private URL urlMock;
	private Session sessionMock;
	private DataCallBack dataCallBackMock;
	
	
	@Before
    public void setup() {
		headerManagerMock = Mockito.mock(HeaderManager.class);
		urlMock = Mockito.mock(URL.class);
		cookieManagerMock = Mockito.mock(CookieManager.class);
		sessionMock = Mockito.mock(Session.class);
		dataCallBackMock = Mockito.mock(DataCallBack.class);
        try {
			http2Connection = new HTTP2Connection("1", true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	

	 @Test
	 public void sendTest() throws Exception {
		 
		 CollectionProperty collProp = new CollectionProperty("HeaderManager.headers", new ArrayList<>());
		 Header header1= new Header("accept", "application/json, text/plain, */*");
		 Header header2= new Header("content-type",	"application/json;charset=UTF-8");
		 collProp.addItem(header1);
		 collProp.addItem(header2);
		 			 
		 Mockito.when(headerManagerMock.getHeaders()).thenReturn(collProp);
		 Mockito.when(cookieManagerMock.getCookieHeaderForURL(urlMock)).thenReturn("TLTSID=F1F77E38627810620014CC0EAD1EEEB4; Path=/; Domain=.sprint.com");
		 Mockito.when(urlMock.toString()).thenReturn("https://www.spring.com");
		 //Mockito.doNothing().when(http2Connection).sendMutExc();
		
		 
		 HTTP2SampleResult sampleResult1 = new HTTP2SampleResult();
		 
		 http2Connection.setSession(sessionMock);
		 http2Connection.send("GET", urlMock, headerManagerMock, cookieManagerMock, null, sampleResult1, true, 0);
		 //HTTP2SampleResult sampleResult2 = new HTTP2SampleResult();
		 //http2Connection.send("POST", urlMock, headerManagerMock, cookieManagerMock, null, sampleResult2, true, 0);
		 		 
	 }
	 
	 @Test
	 public void addPendingResponsesTest(){
		 
		 String request= "Request Example";
		 HTTP2SampleResult pendingResponse = new HTTP2SampleResult();
		 pendingResponse.setPendingResponse(false);
		 pendingResponse.setId(10);
		 HTTP2StreamHandler streamHandler = new HTTP2StreamHandler(http2Connection, urlMock, headerManagerMock, cookieManagerMock, false, pendingResponse);
		 HTTP2SampleResult pendingResponse2 = new HTTP2SampleResult();
		 pendingResponse2.setPendingResponse(true);
		 pendingResponse2.setId(11);		 
		 http2Connection.addPendingResponses(pendingResponse, streamHandler, false);
		 Map<Integer, HTTP2SampleResult> pendings=http2Connection.getPendingResponses();
		 boolean isthere=pendings.containsKey(10);
		 assertEquals(true, isthere);
		 		 
	 }
	 
	 @Test
	 public void syncTest() throws InterruptedException{
		 //DataCallBack dataCallbackMock = new DataCallBack();
		 //Mockito.doNothing().when(dataCallbackMock).getCompletedFuture();
		 //http2Connection.addDataCallbackHandler(dataCallbackMock);
		 http2Connection.sync();
		 
	 }

}
