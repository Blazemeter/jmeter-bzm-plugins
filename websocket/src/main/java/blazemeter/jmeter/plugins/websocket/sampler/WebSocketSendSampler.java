package blazemeter.jmeter.plugins.websocket.sampler;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.EncoderCache;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.util.JOrphanUtils;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;


public class WebSocketSendSampler extends WebSocketAbstractSampler {


	public WebSocketSendSampler() {
        super();
        setName("WebSocket Send Request");
    }

    @Override
    public void setName(String name) {
        if (name != null) {
            setProperty(TestElement.NAME, name);
        }
    }

    @Override
    public String getName() {
        return getPropertyAsString(TestElement.NAME);
    }
  
    
    @Override
    public SampleResult sample(Entry entry) {
    	
    	
    	 	
    	SampleResult sampleResult = new SampleResult();
    	sampleResult.setSampleLabel(getName());
    	
    	String connectionId = getThreadName() + getServer() + getPath() + getPort();
    	
    	try {
			sampleResult.setRequestHeaders(sampleResult.getRequestHeaders() + "URI: " + this.getUri().toString() + "\n\n");
		} catch (URISyntaxException e1) {
			sampleResult.setRequestHeaders(sampleResult.getRequestHeaders() + "URI: " + e1.getMessage() + "\n\n");
		}
    	sampleResult.sampleStart();
    	
    	Handler handler;
		try {
			handler = getConnection (connectionId, sampleResult);
		} catch (Exception e) {
			sampleResult.setSuccessful(false);
			sampleResult.setResponseMessage(e.getMessage());
			sampleResult.setResponseData(e.getStackTrace().toString(),"utf-8");
	    	sampleResult.sampleEnd();
	    	e.printStackTrace();
	    	return sampleResult;
		}
		
		if (handler == null){
			sampleResult.setSuccessful(false);
			sampleResult.setResponseMessage("Connection error");
	    	sampleResult.sampleEnd();
	    	return sampleResult;
		}
    	
    	sampleResult.setDataEncoding(handler.getContentEncoding());
    	
    	handler.initialize();
    	
    	String payload = getPayloadContent();
    	try {
    	
	    	if (!payload.isEmpty()){	
	    		if (!handler.isConnected()){
	    			sampleResult.setSuccessful(false);
		    		sampleResult.setResponseMessage("Connection is closed");
		    		sampleResult.sampleEnd();
		    		return sampleResult;
	    		}
	    		
	    		int responseTimeout;
	    		
	    		try {
	    			responseTimeout = Integer.parseInt(getResponseTimeout());
	    		}catch(NumberFormatException e){
	    			log.warn("Request timeout is not a number; using the default request timeout of " + DEFAULT_RESPONSE_TIMEOUT + "ms");
	                responseTimeout = DEFAULT_RESPONSE_TIMEOUT;
	    		}
	    		
	    		handler.sendMessage(payload);
	    		
	    		if (getWaitUntilResponse()){
    				if (!handler.awaitMessage(responseTimeout, TimeUnit.MILLISECONDS, getResponsePattern())){
    					sampleResult.setSuccessful(false);
    		    		sampleResult.setResponseMessage("Response timeout");
    		    		sampleResult.sampleEnd();
    		    		return sampleResult;
    				}
    				sampleResult.setResponseData(handler.getMessages(), handler.getContentEncoding());
	    		} 
	    	} 
		}catch (Exception e) {
			sampleResult.setSuccessful(false);
    		sampleResult.setResponseMessage(e.getMessage());
    		sampleResult.sampleEnd();
    		return sampleResult;
		}	
		sampleResult.setSuccessful(true);
		sampleResult.setResponseMessage("");
		sampleResult.setRequestHeaders(payload);
		sampleResult.sampleEnd();
		return sampleResult;
	}

	public String getPayloadContent() {
		return getPropertyAsString("payloadContent");
	}

	public boolean getWaitUntilResponse() {
		return getPropertyAsBoolean("waitUntilResponse");
	}

	public String getResponsePattern() {
		return getPropertyAsString("responsePattern");
	}

	public String getResponseTimeout() {
		return getPropertyAsString("responseTimeout");
	}

	public void setPayloadContent(String payloadContent) {
		setProperty("payloadContent", payloadContent);
	}

	public void setWaitUntilResponse(boolean waitUntilResponse) {
		setProperty("waitUntilResponse", waitUntilResponse);
	}

	public void setResponsePattern(String responsePattern) {
		setProperty("responsePattern", responsePattern);
	}

	public void setResponseTimeout(String responseTimeout) {
		setProperty("responseTimeout", responseTimeout);
	}

	private ConfigTestElement getConfigTestElement() {
		ConfigTestElement connectionConfig = (ConfigTestElement) getProperty(this.CONNECT_CONFIG).getObjectValue();
        return connectionConfig;
    }
	
}

