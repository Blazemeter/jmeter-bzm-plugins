package blazemeter.jmeter.plugins.websocket.sampler;

import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;



public class WebSocketCollectorSampler extends WebSocketAbstractSampler {

	private static final Logger log = LoggingManager.getLoggerForClass();

    public WebSocketCollectorSampler() {
        super();
        setName("WebSocket Collector Sampler");
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
			sampleResult.setRequestHeaders("URI: " + this.getUri().toString() + "\n\n");
		} catch (URISyntaxException e1) {
			sampleResult.setRequestHeaders("URI: " + e1.getMessage() + "\n\n");
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
    	
    	int responseTimeout;
		
		try {
			responseTimeout = Integer.parseInt(getResponseTimeout());
		}catch(NumberFormatException e){
			log.warn("Request timeout is not a number; using the default request timeout of " + DEFAULT_RESPONSE_TIMEOUT + "ms");
            responseTimeout = DEFAULT_RESPONSE_TIMEOUT;
		}
    	
    	if (getWaitUntilMessage()){
			try {
				if (!handler.awaitMessage(responseTimeout, TimeUnit.MILLISECONDS, getResponsePattern())){
					sampleResult.setSuccessful(false);
					sampleResult.setResponseMessage("Response timeout");
					sampleResult.setResponseData(handler.getMessages(),handler.getContentEncoding());
					sampleResult.sampleEnd();
					return sampleResult;
				}
			} catch (InterruptedException e) {
				sampleResult.setSuccessful(false);
	    		sampleResult.setResponseMessage(e.getMessage());
	    		sampleResult.setResponseData(handler.getMessages(),handler.getContentEncoding());
	    		sampleResult.sampleEnd();
	    		return sampleResult;
			}
    	}
    	
    	if (getCloseConnection()){
    		handler.close();
    	}
    	
    	try {
			responseTimeout = Integer.parseInt(getResponseTimeout());
			Thread.sleep(responseTimeout);
		}catch(NumberFormatException | InterruptedException e){
			log.warn("Request timeout is not a number; using the default request timeout of " + DEFAULT_RESPONSE_TIMEOUT + "ms");
            responseTimeout = DEFAULT_RESPONSE_TIMEOUT;
		}
    	
    	
    	sampleResult.setSuccessful(true);
		sampleResult.setResponseMessage("");
		sampleResult.setResponseData(handler.getMessages(),handler.getContentEncoding());
		sampleResult.sampleEnd();
    	
    	return sampleResult;
    }

	public String getConnectionId() {
		return getPropertyAsString("connectionId");
	}

	public boolean getWaitUntilMessage() {
		return getPropertyAsBoolean("waitUntilResponse");
	}

	public boolean getCloseConnection() {
		return getPropertyAsBoolean("closeConnection");
	}

	public String getResponsePattern() {
		return getPropertyAsString("responsePattern");
	}

	public String getResponseTimeout() {
		return getPropertyAsString("responseTimeout");
	}

	public void setConnectionId(String connectionId) {
		setProperty("connectionId", connectionId);
	}

	public void setWaitUntilMessage(boolean waitUntilResponse) {
		setProperty("waitUntilResponse", waitUntilResponse);
	}

	public void setCloseConnection(boolean closeConnection) {
		setProperty("closeConnection", closeConnection);
	}

	public void setResponsePattern(String responsePattern) {
		setProperty("responsePattern", responsePattern);
	}

	public void setResponseTimeout(String responseTimeout) {
		setProperty("responseTimeout", responseTimeout);
	}

    

}

