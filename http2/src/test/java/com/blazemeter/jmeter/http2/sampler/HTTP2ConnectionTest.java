package com.blazemeter.jmeter.http2.sampler;

import com.blazemeter.jmeter.http2.sampler.HTTP2Request;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.util.FuturePromise;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.CookieManager;

public class HTTP2ConnectionTest {
	
	private HTTP2Connection http2Connection;
	private HeaderManager headerManagerMock;
	private CookieManager cookieManagerMock;
	private URL urlMock;
	private Session sessionMock;
	
	
	@Before
    public void setup() {
		headerManagerMock = Mockito.mock(HeaderManager.class);
		urlMock = Mockito.mock(URL.class);
		cookieManagerMock = Mockito.mock(CookieManager.class);
		sessionMock=Mockito.mock(Session.class);
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
		
		 
		 HTTP2SampleResult sampleResult = new HTTP2SampleResult();
		 
		 http2Connection.setSession(sessionMock);
		 http2Connection.send("GET", urlMock, headerManagerMock, cookieManagerMock, null, sampleResult, true, 0);
		 // http2Connection.send("POST", urlMock, HeaderManager headerManager, CookieManager cookieManager,
		 //			DataPostContent dataPostContent, HTTP2SampleResult sampleResult, boolean secondaryRequest, int timeout));
		 
	 }
	 
	 @Test
	 public void connectTest(){
		 //connect.get
	 }

}
