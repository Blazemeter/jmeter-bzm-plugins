package blazemeter.jmeter.plugins.websocket.sampler;

import java.io.UnsupportedEncodingException;
import java.net.CookieStore;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.HttpCookie;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.EncoderCache;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import io.inventit.dev.mqtt.paho.MqttWebSocketAsyncClient;

public abstract class WebSocketAbstractSampler extends AbstractSampler implements TestStateListener, ThreadListener {

	private Factory factory;

	protected static final Logger log = LoggingManager.getLoggerForClass();
    
    protected static final int DEFAULT_CONNECTION_TIMEOUT = 20000; //20 sec
    protected static final int DEFAULT_RESPONSE_TIMEOUT = 20000; //20 sec
    
    protected static final String HEADER_MANAGER = "HTTPSampler.header_manager";
	protected static final String COOKIE_MANAGER = "HTTPSampler.cookie_manager"; 
	protected static final String CONNECT_CONFIG = "ConfigTestElement";

    protected static final String PROTOCOL_SCHEME = "WebSocketConnectionConfig.Protocol";
    protected static final String PORT = "WebSocketConnectionConfig.Port"; // $NON-NLS-1$

    protected static final String ARG_VAL_SEP = "="; // $NON-NLS-1$
    protected static final String QRY_SEP = "&"; // $NON-NLS-1$
    protected static final String QRY_PFX = "?"; // $NON-NLS-1$ 

    protected static final String RETURN_NO_SAMPLE = "RETURN_NO_SAMPLE";
    protected static final String CUSTOM_STATUS_CODE= "RETURN_CUSTOM_STATUS.code";
    protected static final String CACHED_MODE_PROPERTY = "cache_manager.cached_resource_mode";

    protected static final String WS_PREFIX = "ws://"; // $NON-NLS-1$
    protected static final String WSS_PREFIX = "wss://"; // $NON-NLS-1$
    protected static final String DEFAULT_PROTOCOL = "ws";  
    protected static final String WS_PROTOCOL = "ws"; // $NON-NLS-1$
    protected static final String WSS_PROTOCOL = "wss"; // $NON-NLS-1$
    protected static final int WS_PROTOCOL_DEFAULT_PORT = 80; // $NON-NLS-1$
    protected static final int WSS_PROTOCOL_DEFAULT_PORT = 443;
    
    
    /** A number to indicate that the port has not been set. */
    protected static final int UNSPECIFIED_PORT = 0;
    protected static final String UNSPECIFIED_PORT_AS_STRING = "0"; // $NON-NLS-1$
    
    protected static final String NON_HTTP_RESPONSE_CODE = "Non HTTP response code";
    protected static final String NON_HTTP_RESPONSE_MESSAGE = "Non HTTP response message";
	
    protected static Map<String, Handler> connectionList;
	
	
	public WebSocketAbstractSampler() {
		super();
		this.factory = new Factory();
	}
	
	public void setFactory (Factory factory){
		this.factory = factory;
	}

	 @Override
	    public void testStarted() {
	        testStarted("unknown");
	    }

	    @Override
	    public void testStarted(String host) {
	        connectionList =  new ConcurrentHashMap<String, Handler>();
	    }

	    @Override
	    public void testEnded() {
	        testEnded("unknown");

	    }

	    @Override
	    public void testEnded(String host) {
	    	for (Handler h : connectionList.values()) {
	            h.close();
	        }  
	    }

	    @Override
	    public void threadStarted() {

	    }

	    @Override
	    public void threadFinished() {

	    }   
	    
	    public Handler getConnection (String connectionId, SampleResult sampleResult) throws Exception{
	    	
	    	if (connectionList.containsKey(connectionId)){
	    		Handler h = connectionList.get(connectionId);
	    		if (h.isConnected())
	    			return h;
	    		else 
	    			connectionList.remove(h);
	    	}
	    	
	    	if(getProtocolWSMQTTComboBox().equals("Mqtt")){
	    		return getMqttConnection(connectionId);
	    	}
	    	else if (getProtocolWSMQTTComboBox().equals("Web Socket")){
	    		return getConnectionWS (connectionId, sampleResult);
	    	}
	    	
	    	return null;
	    }
	    
	    public MqttCallBackImpl getMqttConnection (String connectionId) throws Exception{
	    	final int MAX_RETRIES = 10;
	    	MqttCallBackImpl callBackConnection = null;
	    	IMqttActionListener mConnectionCallback = new IMqttActionListener() {
	    	    @Override
	    	    public void onSuccess(IMqttToken asyncActionToken) {
	    	        log.info("onSuccess " + asyncActionToken);
	    	    }

	    	    @Override
	    	    public void onFailure(IMqttToken asyncActionToken, Throwable ex) {
	    	        log.error("onFailure " + asyncActionToken, ex);
	    	    }
	    	};

    		String mqttUrl = null;
       		mqttUrl = getUri().toString();			


    		int qos = 0;
			String topic = getTopic();
			MemoryPersistence persistence = new MemoryPersistence();
			UUID uuid = UUID.randomUUID();
	        String randomUUIDString = uuid.toString();
	        String clientID = "clientId" + randomUUIDString;
	        
			log.info("PARAMS: "+mqttUrl+" "+topic+" "+clientID);
			IMqttAsyncClient client = null;
			
			
			client = factory.getMqttAsyncClient (getProtocol(), mqttUrl, clientID, persistence);

			log.info("IMqttAsyncClient CREATED");
			callBackConnection = factory.getMqttHandler(client,clientID,getLogLevel(),getContentEncoding()); 
					
			client.setCallback(callBackConnection);
			log.info("CLIENT CALLBACK SETTED: "+clientID+" "+getLogLevel());
			
			int connectionTimeout;
			try {
	    		connectionTimeout = Integer.parseInt(getConnectionTimeout());
	        } catch (NumberFormatException ex) {
	            log.warn("Request timeout is not a number; using the default connection timeout of " + DEFAULT_CONNECTION_TIMEOUT + "ms");
	            connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
	        }
			
			
			MqttConnectOptions connOpts = new MqttConnectOptions();
			log.info("CONNECTION CREATED");
			callBackConnection.addMessage("CONNECTION CREATED");
			connOpts.setConnectionTimeout(connectionTimeout);
			connOpts.setCleanSession(true);
			log.info("CONNECTION CONFIGURED WITH: Timeout: "+connectionTimeout+" cleanSession = TRUE");

			client.connect(connOpts,null,mConnectionCallback);
			log.info("CLIENT CONNECTING...");
			int i=0;
			while (!client.isConnected()&& i<=MAX_RETRIES){
				
				log.info("CONNECTION ATTEMPT: "+i);
				i++;

				Thread.sleep(1000);
			}
			if (i>MAX_RETRIES){
				callBackConnection.addMessage("CLIENT COULD NOT CONNECT");
				return null;
			}

			log.info("CLIENT HAS SUCCESSFULLY CONNECTED");
			callBackConnection.addMessage("CLIENT HAS SUCCESSFULLY CONNECTED");
			
			client.subscribe(topic, qos);
			log.info("CLIENT HAS SUBSCRIBED : TOPIC: "+topic+" QoS: "+qos);
			callBackConnection.addMessage("CLIENT HAS SUBSCRIBED : TOPIC: "+topic+" QoS: "+qos);
 	    	
	    	connectionList.put(connectionId, callBackConnection);
	    	
	    	return callBackConnection;
	    }
	    
	    public WebSocketConnection getConnectionWS (String connectionId, SampleResult sampleResult) throws Exception{
	    	
	    	
	    	
	    	URI uri = getUri();
	    
	    	String closeConnectionPattern = getCloseConnectionPattern();
	    	int connectionTimeout;
	    	
	    	try {
	    		connectionTimeout = Integer.parseInt(getConnectionTimeout());
	        } catch (NumberFormatException ex) {
	            log.warn("Request timeout is not a number; using the default connection timeout of " + DEFAULT_CONNECTION_TIMEOUT + "ms");
	            connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
	        }

	    	SslContextFactory sslContexFactory = new SslContextFactory();
	        sslContexFactory.setTrustAll(true);
	        WebSocketClient webSocketClient = new WebSocketClient(sslContexFactory);
	        WebSocketConnection webSocketConnection = factory.getWebSocketHandler(webSocketClient, closeConnectionPattern, getContentEncoding());
	        
			webSocketClient.start();
	        
	        ClientUpgradeRequest request = new ClientUpgradeRequest();
	        setConnectionHeaders(request, getHeaderManager(), null);
	        setConnectionCookie(webSocketClient, uri, getCookieManager());
	        webSocketClient.connect(webSocketConnection, uri, request);
	        
	        for (Map.Entry<String, List<String>> entry : request.getHeaders().entrySet()){
	        	sampleResult.setRequestHeaders(sampleResult.getRequestHeaders() + entry.getKey() + ": ");
	        	for (String s : entry.getValue()){
	        		sampleResult.setRequestHeaders(sampleResult.getRequestHeaders() + entry.getValue() + " ");
	        	}
	        	sampleResult.setRequestHeaders(sampleResult.getRequestHeaders() + "\n");
	        }
	        
	        sampleResult.setRequestHeaders(sampleResult.getRequestHeaders() + "\n\nCookies: \n\n");
	        
	        for (HttpCookie h : webSocketClient.getCookieStore().getCookies()){
	        	sampleResult.setRequestHeaders(sampleResult.getRequestHeaders() + h);
	        }
			
			webSocketConnection.awaitOpen(connectionTimeout, TimeUnit.MILLISECONDS);
			
	        
	        if (!webSocketConnection.isConnected()){
	    		return null;
	        }
	        
	        connectionList.put(connectionId, webSocketConnection);
	        
	        return webSocketConnection;
	    }
	    
	    /**
	     * Get the URL, built from its component parts.
	     *
	     * <p>
	     * As a special case, if the path starts with "http[s]://",
	     * then the path is assumed to be the entire URL.
	     * </p>
	     *
	     * @return The URL to be requested by this sampler.
	     * @throws MalformedURLException if url is malformed
	     * @throws URISyntaxException 
	     */
	    public URI getUri() throws URISyntaxException {
	        StringBuilder pathAndQuery = new StringBuilder(100);
	        String path = this.getPath();
	        // Hack to allow entire URL to be provided in host field
	        if (path.startsWith(WS_PREFIX)
	                || path.startsWith(WSS_PREFIX)) {
	            return new URI(path);
	        }
	        
	        String domain = getServer();
	        String protocol = getProtocol();
	        
	        // HTTP URLs must be absolute, allow file to be relative
	        if (!path.startsWith("/")) { // $NON-NLS-1$
	           path = "/" + path;
	        }
	
//		    String queryString = getQueryString(getContentEncoding());
//		    if (queryString.length() > 0) {
//		        if (path.contains(QRY_PFX)) {// Already contains a prefix
//		            pathAndQuery.append(QRY_SEP);
//		        } else {
//		            pathAndQuery.append(QRY_PFX);
//		        }
//		        pathAndQuery.append(queryString);
//		    }

	        if (isProtocolDefaultPort()) 
	        	return new URI (protocol + "://" + domain + path);
	        else
	        	return new URI (protocol + "://" + domain + ":" + getPort() + path);	   
//	        // If default port for protocol is used, we do not include port in URL
//	        if (isProtocolDefaultPort()) {
//	        	return new URI(protocol, null, domain, -1, path, null, null);
////	            return new URL(protocol, domain, pathAndQuery.toString());
//	        }
//	        return new URI(protocol, null, domain, getPort(), path, null, null);
//	        return new URL(protocol, domain, getPort(), pathAndQuery.toString());
	        
	    }
	    
	    /**
	     * Tell whether the default port for the specified protocol is used
	     *
	     * @return true if the default port number for the protocol is used, false otherwise
	     */
	    public boolean isProtocolDefaultPort() {
	        final int port = getPortIfSpecified();
	        final String protocol = getProtocol();
	        boolean isDefaultHTTPPort = WebSocketAbstractSampler.WS_PROTOCOL
	                .equalsIgnoreCase(protocol)
	                && port == WebSocketAbstractSampler.WS_PROTOCOL_DEFAULT_PORT;
	        boolean isDefaultHTTPSPort = WebSocketAbstractSampler.WSS_PROTOCOL
	                .equalsIgnoreCase(protocol)
	                && port == WebSocketAbstractSampler.WSS_PROTOCOL_DEFAULT_PORT;
	        return port == UNSPECIFIED_PORT ||
	                isDefaultHTTPPort ||
	                isDefaultHTTPSPort;
	    }
	    
	    /**
	     * Get the port number from the port string, allowing for trailing blanks.
	     *
	     * @return port number or UNSPECIFIED_PORT (== 0)
	     */
	    public int getPortIfSpecified() {
	        String port_s = getPropertyAsString(PORT, UNSPECIFIED_PORT_AS_STRING);
	        try {
	            return Integer.parseInt(port_s.trim());
	        } catch (NumberFormatException e) {
	            return UNSPECIFIED_PORT;
	        }
	    }
	    
	    /**
	     * Get the port; apply the default for the protocol if necessary.
	     *
	     * @return the port number, with default applied if required.
	     */
	    public int getPort() {
	        final int port = getPortIfSpecified();
	        if (port == UNSPECIFIED_PORT) {
	            String prot = getProtocol();
	            if (WSS_PROTOCOL.equalsIgnoreCase(prot)) {
	                return HTTPConstants.DEFAULT_HTTPS_PORT;
	            }
	            if (!WS_PROTOCOL.equalsIgnoreCase(prot)) {
	                log.warn("Unexpected protocol: " + prot);
	            }
	            return HTTPConstants.DEFAULT_HTTP_PORT;
	        }
	        return port;
	    }
	   
	    public void setProtocol(String value) {
	        setProperty(PROTOCOL_SCHEME, value);
	    }

	    public String getProtocol() {        
	        String protocol = getPropertyAsString(PROTOCOL_SCHEME);
	        if (protocol == null || protocol.length() == 0) {
	            return DEFAULT_PROTOCOL;
	        }
	        return protocol;
	    }
	    
	    /**
	     * Gets the QueryString attribute of the UrlConfig object, using the
	     * specified encoding to encode the parameter values put into the URL
	     *
	     * @param contentEncoding the encoding to use for encoding parameter values
	     * @return the QueryString value
	     */
	    public String getQueryString(String contentEncoding) {
	        // Check if the sampler has a specified content encoding
	        if (JOrphanUtils.isBlank(contentEncoding)) {
	            // We use the encoding which should be used according to the HTTP spec, which is UTF-8
	            contentEncoding = EncoderCache.URL_ARGUMENT_ENCODING;
	        }
	        StringBuilder buf = new StringBuilder();
	        PropertyIterator iter = getQueryStringParameters().iterator();
	        boolean first = true;
	        while (iter.hasNext()) {
	            HTTPArgument item = null;
	            /*
	             * N.B. Revision 323346 introduced the ClassCast check, but then used iter.next()
	             * to fetch the item to be cast, thus skipping the element that did not cast.
	             * Reverted to work more like the original code, but with the check in place.
	             * Added a warning message so can track whether it is necessary
	             */
	            Object objectValue = iter.next().getObjectValue();
	            try {
	                item = (HTTPArgument) objectValue;
	            } catch (ClassCastException e) {
	                log.warn("Unexpected argument type: " + objectValue.getClass().getName());
	                item = new HTTPArgument((Argument) objectValue);
	            }
	            final String encodedName = item.getEncodedName();
	            if (encodedName.length() == 0) {
	                continue; // Skip parameters with a blank name (allows use of optional variables in parameter lists)
	            }
	            if (!first) {
	                buf.append(QRY_SEP);
	            } else {
	                first = false;
	            }
	            buf.append(encodedName);
	            if (item.getMetaData() == null) {
	                buf.append(ARG_VAL_SEP);
	            } else {
	                buf.append(item.getMetaData());
	            }

	            // Encode the parameter value in the specified content encoding
	            try {
	                buf.append(item.getEncodedValue(contentEncoding));
	            } catch(UnsupportedEncodingException e) {
	                log.warn("Unable to encode parameter in encoding " + contentEncoding + ", parameter value not included in query string");
	            }
	        }
	        return buf.toString();
	    }

	    public String getProtocolWSMQTTComboBox(){
			return getPropertyAsString("WebSocketConnectionConfig.ProtocolWSMQTTComboBox");
		}
	    
		public String getServer() {
			return getPropertyAsString("WebSocketConnectionConfig.Server");
		}

		public String getConnectionTimeout() {
			return getPropertyAsString("WebSocketConnectionConfig.ConnectionTimeout");
		}
		
		public String getPath() {
			return getPropertyAsString("WebSocketConnectionConfig.Path");
		}
		
		public String getTopic() {
			return getPropertyAsString("WebSocketConnectionConfig.Topic");
		}
		
		public String getImplementation() {
			return getPropertyAsString("WebSocketConnectionConfig.Implementation");
		}

		public Arguments getQueryStringParameters() {
			Arguments args = (Arguments) getProperty("WebSocketConnectionConfig.HTTPRequest.ARGUMENTS").getObjectValue();
	        return args;
		}

		public Arguments getQueryStringPatterns() {
			Arguments args = (Arguments) getProperty("WebSocketConnectionConfig.ResponsePatterns.ARGUMENTS").getObjectValue();
	        return args;
		}

	    public String getContentEncoding() {
	        return getPropertyAsString("WebSocketConnectionConfig.Encoding");
	    }
	    
	    public String getCloseConnectionPattern() {
			return getPropertyAsString("WebSocketConnectionConfig.CloseConnectionPattern");
		}

	    private HeaderManager getHeaderManager() {
	    	HeaderManager headerManager = (HeaderManager) getProperty(this.HEADER_MANAGER).getObjectValue();
	        return headerManager;
	    }

	    public CookieManager getCookieManager() {
	        return (CookieManager) getProperty(this.COOKIE_MANAGER).getObjectValue();
	    }
	    
	    public String getLogLevel(){
			return getPropertyAsString("WebSocketConnectionConfig.LogLevel");
		}
	    
		protected void setConnectionHeaders(ClientUpgradeRequest request, HeaderManager headerManager, CacheManager cacheManager) {
	        if (headerManager != null) {
	            CollectionProperty headers = headerManager.getHeaders();
	            PropertyIterator p = headers.iterator();
	            if (headers != null) {
	            	while (p.hasNext()){
	            		JMeterProperty jMeterProperty = p.next();
	            		org.apache.jmeter.protocol.http.control.Header header
	                    = (org.apache.jmeter.protocol.http.control.Header)
	                            jMeterProperty.getObjectValue();
	                    String n = header.getName();
	                    if (! HTTPConstants.HEADER_CONTENT_LENGTH.equalsIgnoreCase(n)){
	                        String v = header.getValue();
	                		request.setHeader(n, v);
	                    }
	            	}
	            }
	        }
	        if (cacheManager != null){
	        }
	    }
		
		protected String setConnectionCookie(WebSocketClient wsClient, URI uri, CookieManager cookieManager) throws MalformedURLException {
	        String cookieHeader = null;
	        if (cookieManager != null) {
	        	URL url;
	        	
	            String path = this.getPath();
	            String domain = getServer();    
	            
	            // HTTP URLs must be absolute, allow file to be relative
	            if (!path.startsWith("/")) { // $NON-NLS-1$
	            	path= "/" + path; // $NON-NLS-1$
	            }
	            if (!path.endsWith("/")) { // $NON-NLS-1$
	            	path= path + "/"; // $NON-NLS-1$
	            }
	            if (uri.getQuery() != null)
	            	path = path + uri.getQuery();
	        	
	        	String protocol = getProtocol().equalsIgnoreCase(WS_PROTOCOL) ? "http" : "https";
	        	if (isProtocolDefaultPort()) {
	                url = new URL(protocol, domain, path);
	            }
	        	url = new URL(protocol, domain, getPort(), path);
	        	
	        	
	            cookieHeader = cookieManager.getCookieHeaderForURL(url);
	            if (cookieHeader != null) {
		            for (String s : cookieHeader.split(";")){
		            	HttpCookie c = new HttpCookie(s.split("=")[0], s.split("=")[1]);
		            	wsClient.getCookieStore().add(uri, c);
		            }
	            }
	        }
	        return cookieHeader;
	    }
		
		private int getPortFromHostHeader(String hostHeaderValue, int defaultValue) {
	        String[] hostParts = hostHeaderValue.split(":");
	        if (hostParts.length > 1) {
	            String portString = hostParts[hostParts.length - 1];
	            if (Pattern.compile("\\d+").matcher(portString).matches()) {
	                return Integer.parseInt(portString);
	            }
	        }
	        return defaultValue;
	    }
		
		@Override
		public void addTestElement(TestElement el) {
			if (el instanceof HeaderManager) {
				setHeaderManager((HeaderManager) el);
			} else if (el instanceof CookieManager) {
				setCookieManager((CookieManager) el);
			} else {
				super.addTestElement(el);
			}
		}
		
		public void setHeaderManager(HeaderManager value) {
			HeaderManager mgr = getHeaderManager();
			if (mgr != null) {
				value = mgr.merge(value, true);
				if (log.isDebugEnabled()) {
					log.debug("Existing HeaderManager '" + mgr.getName() + "' merged with '" + value.getName() + "'");
					for (int i = 0; i < value.getHeaders().size(); i++) {
						log.debug("    " + value.getHeader(i).getName() + "=" + value.getHeader(i).getValue());
					}
				}
			}
			setProperty(new TestElementProperty(HEADER_MANAGER, value));
		}
		
		private void setCookieManagerProperty(CookieManager value) {
			setProperty(new TestElementProperty(COOKIE_MANAGER, value));
		}
		
		public void setCookieManager(CookieManager value) {
	        CookieManager mgr = getCookieManager();
	        if (mgr != null) {
	            log.warn("Existing CookieManager " + mgr.getName() + " superseded by " + value.getName());
	        }
	        setCookieManagerProperty(value);
	    }
	
}
