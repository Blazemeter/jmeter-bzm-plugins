package blazemeter.jmeter.plugins.websocket.sampler;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.protocol.HTTP;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Deprecated
public class WebSocketConnectionSampler extends WebSocketAbstractSampler {


    /**
	 * 
	 */
	private static final long serialVersionUID = -1989536169021169661L;

	public WebSocketConnectionSampler() {
        super();
        setName("WebSocket Connection Sampler");
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
        sampleResult.setDataEncoding(getContentEncoding());
    	String connectionId = getThreadName() + getConnectionId();
    	URI uri;
    	
    	try {
			uri = getUri();
		} catch (URISyntaxException e) {
			sampleResult.setSuccessful(false);
			sampleResult.setResponseMessage(e.getMessage());
			sampleResult.setResponseData(e.getStackTrace().toString(),"utf-8");
			return sampleResult;
		}
    	
    	String closeConnectionPattern = getCloseConnectionPattern();
    	int connectionTimeout;
    	
    	try {
    		connectionTimeout = Integer.parseInt(getConnectionTimeout());
        } catch (NumberFormatException ex) {
            log.warn("Request timeout is not a number; using the default connection timeout of " + DEFAULT_CONNECTION_TIMEOUT + "ms");
            connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
        }
    	
    	
    	if (connectionList.containsKey(connectionId)){
    		sampleResult.setSuccessful(false);
    		sampleResult.setResponseMessage("Connection already exists");
    		return sampleResult;
    	}
    	
    	sampleResult.sampleStart();
    	
    	SslContextFactory sslContexFactory = new SslContextFactory();
        sslContexFactory.setTrustAll(true);
        WebSocketClient webSocketClient = new WebSocketClient(sslContexFactory);
        WebSocketConnection webSocketConnection = new WebSocketConnection(webSocketClient, closeConnectionPattern, getContentEncoding());
        
        try {
			webSocketClient.start();
		} catch (Exception e) {
			sampleResult.setSuccessful(false);
			sampleResult.setResponseMessage(e.getMessage());
			sampleResult.setResponseData(e.getStackTrace().toString(),"utf-8");
	    	sampleResult.sampleEnd();
	    	return sampleResult;
		}
        
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        setConnectionHeaders(request, getHeaderManager(), null);
        try {
			setConnectionCookie(request, uri, getCookieManager());
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        
        try {
			webSocketClient.connect(webSocketConnection, uri, request);
		} catch (IOException e) {
			sampleResult.setSuccessful(false);
			sampleResult.setResponseMessage(e.getMessage());
	    	sampleResult.sampleEnd();
	    	return sampleResult;
		}
        long start = System.currentTimeMillis();
        
        try {
			webSocketConnection.awaitOpen(connectionTimeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        if (!webSocketConnection.isConnected()){
        	sampleResult.setSuccessful(false);
			sampleResult.setResponseMessage("Connection timeout");
	    	sampleResult.sampleEnd();
	    	return sampleResult;
        }
        
        connectionList.put(connectionId, webSocketConnection);
        
        sampleResult.sampleEnd();
        sampleResult.setSuccessful(true);
        sampleResult.setResponseMessage("Connection open");
    	
        return sampleResult;
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
            pathAndQuery.append("/"); // $NON-NLS-1$
        }
        pathAndQuery.append(path);

	    String queryString = getQueryString(getContentEncoding());
	    if (queryString.length() > 0) {
	        if (path.contains(QRY_PFX)) {// Already contains a prefix
	            pathAndQuery.append(QRY_SEP);
	        } else {
	            pathAndQuery.append(QRY_PFX);
	        }
	        pathAndQuery.append(queryString);
	    }
	    
        // If default port for protocol is used, we do not include port in URL
        if (isProtocolDefaultPort()) {
        	return new URI(protocol, null, domain, -1, path, queryString, null);
//            return new URL(protocol, domain, pathAndQuery.toString());
        }
        return new URI(protocol, null, domain, getPort(), path, queryString, null);
//        return new URL(protocol, domain, getPort(), pathAndQuery.toString());
    }
    
    /**
     * Tell whether the default port for the specified protocol is used
     *
     * @return true if the default port number for the protocol is used, false otherwise
     */
    public boolean isProtocolDefaultPort() {
        final int port = getPortIfSpecified();
        final String protocol = getProtocol();
        boolean isDefaultHTTPPort = HTTPConstants.PROTOCOL_HTTP
                .equalsIgnoreCase(protocol)
                && port == HTTPConstants.DEFAULT_HTTP_PORT;
        boolean isDefaultHTTPSPort = HTTPConstants.PROTOCOL_HTTPS
                .equalsIgnoreCase(protocol)
                && port == HTTPConstants.DEFAULT_HTTPS_PORT;
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

	public String getServer() {
		return getPropertyAsString("server");
	}
	
	public void setServer(String server) {
		setProperty("server", server);
	}

	public String getConnectionTimeout() {
		return getPropertyAsString("connectionTimeout");
	}
	
	public void setConnectionTimeout(String connectionTimeout) {
		setProperty("connectionTimeout", connectionTimeout);
	}

	public String getPath() {
		return getPropertyAsString("path");
	}
	
	public void setPath(String path) {
        setProperty("path", path);
    }
	
	public void setContextPath(String contextPath) {
        setProperty("contextPath", contextPath);
    }

	public String getConnectionId() {
		return getPropertyAsString("connectionId");
	}

	public void setConnectionId(String connectionId) {
		setProperty("connectionId", connectionId);
	}
	
	public String getImplementation() {
		return getPropertyAsString("implementation");
	}

	public void setImplementation(String implementation) {
		setProperty("implementation", implementation);
	}

	public Arguments getQueryStringParameters() {
		Arguments args = (Arguments) getProperty("queryStringParameters").getObjectValue();
        return args;
	}
	
	public void setQueryStringParameters(Arguments queryStringParameters) {
		setProperty(new TestElementProperty("queryStringParameters", queryStringParameters));	
	}

	public Arguments getQueryStringPatterns() {
		Arguments args = (Arguments) getProperty("queryStringPatterns").getObjectValue();
        return args;
	}
	
	public void setQueryStringPatterns(Arguments queryStringPatterns) {
		setProperty(new TestElementProperty("queryStringPatterns", queryStringPatterns));	
	}
    
    public void setPort(String port) {
        setProperty("serverPort", port);
    }

    public void setContentEncoding(String contentEncoding) {
        setProperty("contentEncoding", contentEncoding);
    }

    public String getContentEncoding() {
        return getPropertyAsString("contentEncoding", "UTF-8");
    }

    private HeaderManager getHeaderManager() {
    	HeaderManager headerManager = (HeaderManager) getProperty(this.HEADER_MANAGER).getObjectValue();
        return headerManager;
    }

    public CookieManager getCookieManager() {
        return (CookieManager) getProperty(this.COOKIE_MANAGER).getObjectValue();
    }
    
	public String getCloseConnectionPattern() {
		return getPropertyAsString("closeConnectionPattern");
	}

	public void setCloseConnectionPattern(String implementation) {
		setProperty("closeConnectionPattern", implementation);
	}   
	
	protected void setConnectionHeaders(ClientUpgradeRequest request, HeaderManager headerManager, CacheManager cacheManager) {
        if (headerManager != null) {
            CollectionProperty headers = headerManager.getHeaders();
            if (headers != null) {
                for (JMeterProperty jMeterProperty : headers) {
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
	
	protected String setConnectionCookie(ClientUpgradeRequest request, URI uri, CookieManager cookieManager) throws MalformedURLException {
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
            
        	path = path + uri.getQuery();
        	
        	String protocol = getProtocol().equalsIgnoreCase(WS_PROTOCOL) ? "http" : "https";
        	if (isProtocolDefaultPort()) {
                url = new URL(protocol, domain, path);
            }
        	url = new URL(protocol, domain, getPort(), path);
        	
        	
            cookieHeader = cookieManager.getCookieHeaderForURL(uri.toURL());
            if (cookieHeader != null) {
                request.setHeader(HTTPConstants.HEADER_COOKIE, cookieHeader);
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

