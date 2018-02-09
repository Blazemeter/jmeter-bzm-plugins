package com.blazemeter.jmeter.http2.sampler;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.util.*;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;
import org.eclipse.jetty.http.HttpFields;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


public class HTTP2Request extends AbstractSampler implements TestStateListener, ThreadListener {
    private static final long serialVersionUID = 5859387434748163229L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    public static final String DEFAULT_RESPONSE_TIMEOUT = "20000"; //20 sec

    public static final String METHOD = "HTTP2Sampler.method";
    public static final String PROTOCOL_SCHEME = "HTTP2Sampler.scheme";
    public static final String IMPLEMENTATION = "HTTP2Sampler.implementation"; // $NON-NLS-1$
    
    public static final String PORT = "HTTPSampler.port"; // $NON-NLS-1$

    // Must be private, as the file list needs special handling
    private static final String FILE_ARGS = "HTTPsampler.Files"; // $NON-NLS-1$

    public static final String DO_MULTIPART_POST = "HTTPSampler.DO_MULTIPART_POST"; // $NON-NLS-1$


    public static final String DEFAULT_METHOD = "GET";
    public static final String RETURN_NO_SAMPLE = "RETURN_NO_SAMPLE";
    public static final String CUSTOM_STATUS_CODE= "RETURN_CUSTOM_STATUS.code";
    public static final String CACHED_MODE_PROPERTY = "cache_manager.cached_resource_mode";
    
    public static final String HEADER_MANAGER = "HTTP2Request.header_manager"; // $NON-NLS-1$
    
    public static final String COOKIE_MANAGER = "HTTP2Request.cookie_manager"; // $NON-NLS-1$
    
    public static final String CACHE_MANAGER = "HTTP2Request.cache_manager"; // $NON-NLS-1$

    private static final String HTTP_PREFIX = HTTPConstants.PROTOCOL_HTTP+"://"; // $NON-NLS-1$
    private static final String HTTPS_PREFIX = HTTPConstants.PROTOCOL_HTTPS+"://"; // $NON-NLS-1$
    private static final String DEFAULT_PROTOCOL = HTTPConstants.PROTOCOL_HTTPS;
    
    
    /** A number to indicate that the port has not been set. */
    public static final int UNSPECIFIED_PORT = 0;
    public static final String UNSPECIFIED_PORT_AS_STRING = "0"; // $NON-NLS-1$
    
    protected static final String NON_HTTP_RESPONSE_CODE = "Non HTTP response code";

    protected static final String NON_HTTP_RESPONSE_MESSAGE = "Non HTTP response message";
    
    public static final String IP_SOURCE = "HTTP2Request.ipSource"; // $NON-NLS-1$

    public static final String IP_SOURCE_TYPE = "HTTP2Request.ipSourceType"; // $NON-NLS-1$
    
    // Embedded URLs must match this RE (if provided)
    public static final String EMBEDDED_URL_RE = "HTTPSampler.embedded_url_re"; // $NON-NLS-1$
    
    // Store MD5 hash instead of storing response
    public static final String MD5 = "HTTPSampler.md5"; // $NON-NLS-1$
    
    public static final String EMBEDDED_RESOURCES = "HTTPSampler.embedded_resources"; // $NON-NLS-1$
    
    public static final int SOURCE_TYPE_DEFAULT = HTTPSamplerBase.SourceType.HOSTNAME.ordinal();
    
    public static final String ARGUMENTS = "HTTP2Request.Arguments"; // $NON-NLS-1$
    
    public static final String POST_BODY_RAW = "HTTP2Request.postBodyRaw"; // TODO - belongs elsewhere

    public static final boolean POST_BODY_RAW_DEFAULT = false;
    
    public static final String DOMAIN = "HTTP2Request.domain"; // $NON-NLS-1$
    
    public static final String RESPONSE_TIMEOUT = "HTTP2Request.response_timeout"; // $NON-NLS-1$
    
    public static final String FOLLOW_REDIRECTS = "HTTP2Request.follow_redirects"; // $NON-NLS-1$

    public static final String AUTO_REDIRECTS = "HTTP2Request.auto_redirects"; // $NON-NLS-1$
    
    public static final String SYNCREQUEST = "HTTP2Request.sync_request"; // $NON-NLS-1$

    public static final String PROTOCOL = "HTTP2Request.protocol"; // $NON-NLS-1$
    
    public static final String REQUEST_ID = "HTTP2Request.request_id"; // $NON-NLS-1$
    
    /** This is the encoding used for the content, i.e. the charset name, not the header "Content-Encoding" */
    public static final String CONTENT_ENCODING = "HTTP2Request.contentEncoding"; // $NON-NLS-1$

    public static final String PATH = "HTTP2Request.path"; // $NON-NLS-1$
    
    protected static final int MAX_FRAME_DEPTH = JMeterUtils.getPropDefault("httpsampler.max_frame_depth", 5); // $NON-NLS-1$
    
 
    
    private static Map<String, HTTP2Connection> connectionList;
    
    private HTTP2Connection http2Connection;
    

    public HTTP2Request() {
        super();
        setName("HTTP2 Request");
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
        return sample();
    }
    
    /**
     * Perform a sample, and return the results
     *
     * @return results of the sampling
     */
    public SampleResult sample() {
        SampleResult res = null;
        try {
        	URL url= getUrl();
        	// Create Sample Result
        	HTTP2SampleResult sampleResult =new HTTP2SampleResult(url, getMethod());
        	setConnection(url, sampleResult);
            res = sample(url, getMethod(), false, 0, getConnection(), sampleResult);
            if (res != null) {
                res.setSampleLabel(getName());
            }
            return res;
        } catch (Exception e) {
        	return res;
        }
    }
    
    /**
     * Populates the provided HTTPSampleResult with details from the Exception.
     * Does not create a new instance, so should not be used directly to add a subsample.
     *
     * @param e
     *            Exception representing the error.
     * @param res
     *            SampleResult to be modified
     * @return the modified sampling result containing details of the Exception.
     */
    protected HTTP2SampleResult errorResult(Throwable e, HTTP2SampleResult res) {
        res.setSampleLabel(res.getSampleLabel());
        res.setDataType(SampleResult.TEXT);
        ByteArrayOutputStream text = new ByteArrayOutputStream(200);
        e.printStackTrace(new PrintStream(text));
        res.setResponseData(text.toByteArray());
        res.setResponseCode(NON_HTTP_RESPONSE_CODE+": " + e.getClass().getName());
        res.setResponseMessage(NON_HTTP_RESPONSE_MESSAGE+": " + e.getMessage());
        res.setSuccessful(false);
        res.setPendingResponse(false);
        return res;
    }


    /** Retorna el cuerpo que sera enviado en el post, de acuerdo a los parametros que obtenga */

    public DataPostContent createPostContent(String method){
        DataPostContent dpc = null;
        if (method.equals("POST")) {
            dpc = new DataPostContent();
            //TODO set things
            Arguments fafa = getArguments();
            String valor = "";
            for (JMeterProperty jmp : fafa) {
                valor = ((HTTPArgument) jmp.getObjectValue()).getEncodedValue();
            }
            dpc.setPayload(valor.getBytes());
            
            // TODO Code to send a file, need to figure out where is goes

            dpc.setDataPath(getProperty(HTTP2Request.PATH).getStringValue());
        }
        return dpc;
    }


    String getRequestIdForConnectionsMap() {
        return getThreadName() + getRequestId();
    }

    protected HTTP2SampleResult sample(URL url, String method, boolean followRedirects, int depth, HTTP2Connection http2Connection, HTTP2SampleResult sampleResult) {
    	
        sampleResult.setEmbebedResults(isEmbeddedResources());
        sampleResult.setEmbeddedUrlRE(getEmbeddedUrlRE());
        
        try {
            DataPostContent dpc = createPostContent(method);
            int timeout=Integer.parseInt(DEFAULT_RESPONSE_TIMEOUT);
            if (!getResponseTimeout().equals("")){
            	timeout=Integer.parseInt(getResponseTimeout());
            }
        	http2Connection.send(method, url, getHeaderManager(), getCookieManager(), dpc, sampleResult, false, timeout);

            final CacheManager cacheManager = getCacheManager();
            if (cacheManager != null && HTTPConstants.GET.equalsIgnoreCase(method)) {
            	// TODO implement cache Manager
               
            }
            if (isSyncRequest()){
                for (HTTP2Connection h : this.connectionList.values()){
                    if (h.getConnectionId().contains(getThreadName())){
                        h.sync();
                    }
                }
                for (HTTP2Connection h : this.connectionList.values()){
                    if (h.getConnectionId().contains(getThreadName())){
                        for (HTTP2SampleResult r : http2Connection.getPendingResponses().values()) {
                    		if (!r.isSecondaryRequest()){
                    			if (sampleResult.getId()!=r.getId()){
                    				//Is not the response of a sync request and is not a embedded result
                    				sampleResult.addRawSubResult(r);
                    			}
                            } 
                    		
                    		saveConnectionCookies(r.getHttpFieldsResponse(), r.getURL(), getCookieManager());
                    		
                        }
                    }
                }
                http2Connection.reset();
            } else {
                //request was sent successfully
                sampleResult.setSuccessful(true);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            return errorResult(e, sampleResult);
        }

        return sampleResult;
    }
    
    public HTTP2Connection getConnection(){
    	return http2Connection;
    }

    public void addConnection(String id, HTTP2Connection connection){
    	connectionList.put(id, connection);
    }
    
	public void setConnection(URL url, HTTP2SampleResult sampleResult) {

    	String host = url.getHost().replaceAll("\\[", "").replaceAll("\\]", "");
    	int port = url.getPort();
    	
    	if (port == -1)
    		port = url.getDefaultPort();
        	
    	String connectionId = getThreadName() + host + port;

    	long connectTime=0;
    	if (connectionList.containsKey(connectionId)){
    		http2Connection = connectionList.get(connectionId);
    		if (http2Connection.isClosed())
				try {
					long startConnectTime= sampleResult.currentTimeInMillis();
					http2Connection.connect(host, port);
					long endConnectTime=sampleResult.currentTimeInMillis();
					connectTime= endConnectTime - startConnectTime;
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	} else {
    		try {
        		//TODO manejar cuando no es SSL
    			http2Connection = new HTTP2Connection(connectionId,true);
    			long startConnectTime= sampleResult.currentTimeInMillis();
    			http2Connection.connect(host, port);
    			long endConnectTime=sampleResult.currentTimeInMillis();
    			connectTime= endConnectTime - startConnectTime;
    			this.connectionList.put(connectionId, http2Connection);
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			http2Connection = null;
    			e.printStackTrace();
    		}
        	sampleResult.setConnectTime(connectTime);
    	}

	}


    public String getMethod() {
        return getPropertyAsString(METHOD);
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
     */
    public URL getUrl() throws MalformedURLException {
        StringBuilder pathAndQuery = new StringBuilder(100);
        String path = this.getContextPath();
        // Hack to allow entire URL to be provided in host field
        if (path.startsWith(HTTP_PREFIX)
                || path.startsWith(HTTPS_PREFIX)) {
            return new URL(path);
        }
        
        String domain = getDomain();
        String protocol = getProtocol();
        
        // HTTP URLs must be absolute, allow file to be relative
        if (!path.startsWith("/")) { // $NON-NLS-1$
            pathAndQuery.append("/"); // $NON-NLS-1$
        }
        pathAndQuery.append(path);

        // If default port for protocol is used, we do not include port in URL
        if (isProtocolDefaultPort()) {
            return new URL(protocol, domain, pathAndQuery.toString());
        }
        return new URL(protocol, domain, getPort(), pathAndQuery.toString());
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
            if (HTTPConstants.PROTOCOL_HTTPS.equalsIgnoreCase(prot)) {
                return HTTPConstants.DEFAULT_HTTPS_PORT;
            }
            if (!HTTPConstants.PROTOCOL_HTTP.equalsIgnoreCase(prot)) {
                log.warn("Unexpected protocol: " + prot);
                // TODO - should this return something else?
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
     * Determine if the file should be sent as the entire Content body,
     * i.e. without any additional wrapping.
     *
     * @return true if specified file is to be sent as the body,
     * i.e. there is a single file entry which has a non-empty path and
     * an empty Parameter name.
     */
    public boolean getSendFileAsPostBody() {
        // If there is one file with no parameter name, the file will
        // be sent as post body.
        HTTPFileArg[] files = getHTTPFiles();
        return (files.length == 1)
                && (files[0].getPath().length() > 0)
                && (files[0].getParamName().length() == 0);
    }


    /**
     * Determine if we should use multipart/form-data or
     * application/x-www-form-urlencoded for the post
     *
     * @return true if multipart/form-data should be used and method is POST
     */
    public boolean getUseMultipartForPost() {
        // We use multipart if we have been told so, or files are present
        // and the files should not be send as the post body
        HTTPFileArg[] files = getHTTPFiles();
        return HTTPConstants.POST.equals(getMethod())
                && (getDoMultipartPost() || (files.length > 0 && !getSendFileAsPostBody()));
    }

    public String getDomain() {
        return getPropertyAsString(DOMAIN);
    }

    public String getResponseTimeout() {
        return getPropertyAsString(RESPONSE_TIMEOUT);
    }

    public String getContextPath() {
        return getPropertyAsString(PATH);
    }

    public boolean getDoMultipartPost() {
        return getPropertyAsBoolean(DO_MULTIPART_POST, false);
    }

    public Arguments getArguments() {
        return (Arguments) getProperty(ARGUMENTS).getObjectValue();
    }

    private HTTPFileArgs getHTTPFileArgs() {
        return (HTTPFileArgs) getProperty(FILE_ARGS).getObjectValue();
    }

    public HTTPFileArg[] getHTTPFiles() {
        final HTTPFileArgs fileArgs = getHTTPFileArgs();
        return fileArgs == null ? new HTTPFileArg[] {} : fileArgs.asArray();
    }

    public String getRequestId() {
        return getPropertyAsString(REQUEST_ID);
    }


    public String getContentEncoding() {
        return getPropertyAsString(CONTENT_ENCODING, "UTF-8");
    }
    
    public Boolean isFollowRedirects() {
        return getPropertyAsBoolean(FOLLOW_REDIRECTS);
    }

    public Boolean isAutoRedirect() {
        return getPropertyAsBoolean(AUTO_REDIRECTS);
    }
    
    public Boolean isEmbeddedResources() {
        return getPropertyAsBoolean(EMBEDDED_RESOURCES);
    }
    
    public Boolean isSyncRequest() {
        return getPropertyAsBoolean(SYNCREQUEST);
    }

    public HeaderManager getHeaderManager() {
        return (HeaderManager) getProperty(HTTP2Request.HEADER_MANAGER).getObjectValue();
    }

    /**
     * Get the regular expression URLs must match.
     *
     * @return regular expression (or empty) string
     */
    public String getEmbeddedUrlRE() {
        return getPropertyAsString(EMBEDDED_URL_RE, "");
    }

    @Override
    public void testStarted() {
        testStarted("unknown");
    }

    @Override
    public void testStarted(String host) {
       connectionList = new ConcurrentHashMap<String, HTTP2Connection>();
    }

    @Override
    public void testEnded() {
        testEnded("unknown");

    }

    @Override
    public void testEnded(String host) {
    	for (HTTP2Connection connection : connectionList.values()) {
            try {
				connection.disconnect();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    
    @Override
    public void addTestElement(TestElement el) {
        if (el instanceof HeaderManager) {
            setHeaderManager((HeaderManager) el);
        } else if (el instanceof CookieManager) {
            setCookieManager((CookieManager) el);
        } else if (el instanceof CacheManager) {
            setCacheManager((CacheManager) el);
        }
        else {
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
    
    // private method to allow AsyncSample to reset the value without performing checks
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

    public CookieManager getCookieManager() {
        return (CookieManager) getProperty(COOKIE_MANAGER).getObjectValue();
    }
    
    public void setCacheManager(CacheManager value) {
        CacheManager mgr = getCacheManager();
        if (mgr != null) {
            log.warn("Existing CacheManager " + mgr.getName() + " superseded by " + value.getName());
        }
        setCacheManagerProperty(value);
    }
    
    // private method to allow AsyncSample to reset the value without performing checks
    private void setCacheManagerProperty(CacheManager value) {
        setProperty(new TestElementProperty(CACHE_MANAGER, value));
    }
    
    public CacheManager getCacheManager() {
        return (CacheManager) getProperty(CACHE_MANAGER).getObjectValue();
    }

    @Override
    public void threadStarted() {

    }

    @Override
    public void threadFinished() {

    }
    
    public void saveConnectionCookies(HttpFields hdrsResponse, URL url, CookieManager cookieManager) {
        if (cookieManager != null) {
        	List<String> hdrs = hdrsResponse.getValuesList(HTTPConstants.HEADER_SET_COOKIE);
        	Iterator iter=hdrs.iterator();
        	while (iter.hasNext())
        		cookieManager.addCookieFromHeader((String) iter.next(), url);
        }

    }

    

    public void setEmbeddedResources(boolean embeddedResources) {
        setProperty(EMBEDDED_RESOURCES, embeddedResources, false);
    }
	
    public void setEmbeddedUrlRE(String regex) {
        setProperty(new StringProperty(EMBEDDED_URL_RE, regex));
    }

}

