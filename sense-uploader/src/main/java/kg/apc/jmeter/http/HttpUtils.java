package kg.apc.jmeter.http;

import kg.apc.jmeter.notifier.StatusNotifierCallback;
import net.sf.json.JSON;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.LinkedList;

/**
 * Class for working with HTTP requests
 */
public class HttpUtils {

    protected static final Logger log = LoggingManager.getLoggerForClass();
    protected final static int TIMEOUT = 5;

    protected final AbstractHttpClient httpClient;
    protected final StatusNotifierCallback notifier;
    protected final String address;
    protected final String dataAddress;

    public HttpUtils(StatusNotifierCallback notifier, String address, String dataAddress) {
        this.notifier = notifier;
        this.address = address;
        this.dataAddress = dataAddress;
        this.httpClient = createHTTPClient();
    }

    /**
     * Create Get Request
     */
    public HttpGet createGet(String uri) {
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeader("Content-Type", "application/json");
        return httpGet;
    }

    /**
     * Create Post Request with json body
     */
    public HttpPost createPost(String uri, String data) {
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader("Content-Type", "application/json");
        HttpEntity entity = new StringEntity(data, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        return httpPost;
    }

    /**
     * Create Post Request with FormBodyPart body
     */
    public HttpPost createPost(String uri, LinkedList<FormBodyPart> partsList) {
        HttpPost postRequest = new HttpPost(uri);
        MultipartEntity multipartRequest = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        for (FormBodyPart part : partsList) {
            multipartRequest.addPart(part);
        }
        postRequest.setEntity(multipartRequest);
        return postRequest;
    }

    /**
     * Create Patch Request
     */
    public HttpPatch createPatch(String url, JSON data) {
        HttpPatch patch = new HttpPatch(url);
        patch.setHeader("Content-Type", "application/json");

        String string = data.toString(1);
        try {
            patch.setEntity(new StringEntity(string, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return patch;
    }

    /**
     * Execute Http request and verify response
     * @param request - HTTP Request
     * @param expectedCode - expected response code
     * @return - response in JSONObject
     */
    public JSONObject queryObject(HttpRequestBase request, int expectedCode) throws IOException {
        JSON res = query(request, expectedCode);
        if (!(res instanceof JSONObject)) {
            throw new IOException("Unexpected response: " + res);
        }
        return (JSONObject) res;
    }

    /**
     * Execute Http request and response code
     * @param request - HTTP Request
     * @param expectedCode - expected response code
     * @return - response in JSONObject
     */
    public JSON query(HttpRequestBase request, int expectedCode) throws IOException {
        log.info("Requesting: " + request);
        addRequiredHeader(request);

        HttpParams requestParams = request.getParams();
        requestParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, TIMEOUT * 1000);
        requestParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT * 1000);

        synchronized (httpClient) {
            String response;
            try {
                HttpResponse result = httpClient.execute(request);

                int statusCode = result.getStatusLine().getStatusCode();

                response = getResponseEntity(result);

                if (statusCode != expectedCode) {

                    notifier.notifyAbout("Response with code " + statusCode + ": " + extractErrorMessage(response));
                    throw new IOException("API responded with wrong status code: " + statusCode);
                } else {
                    log.debug("Response: " + response);
                }
            } finally {
                request.abort();
            }

            if (response == null || response.isEmpty()) {
                return JSONNull.getInstance();
            } else {
                return JSONSerializer.toJSON(response, new JsonConfig());
            }
        }
    }

    protected String extractErrorMessage(String response) {
        return response;
    }

    protected void addRequiredHeader(HttpRequestBase httpRequestBase) {
        // NOOP
    }

    private String getResponseEntity(HttpResponse result) throws IOException {
        HttpEntity entity = result.getEntity();
        if (entity == null) {
            log.debug("Null response entity");
            return null;
        }

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            entity.writeTo(bos);
            byte[] bytes = bos.toByteArray();
            if (bytes == null) {
                bytes = "null".getBytes();
            }
            String response = new String(bytes);
            log.debug("Response with code " + result + ": " + response);
            return response;
        } finally {
            InputStream content = entity.getContent();
            if (content != null) {
                content.close();
            }
        }
    }

    private static AbstractHttpClient createHTTPClient() {
        AbstractHttpClient client = new DefaultHttpClient();
        String proxyHost = System.getProperty("https.proxyHost", "");
        if (!proxyHost.isEmpty()) {
            int proxyPort = Integer.parseInt(System.getProperty("https.proxyPort", "-1"));
            log.info("Using proxy " + proxyHost + ":" + proxyPort);
            HttpParams params = client.getParams();
            HttpHost proxy = new HttpHost(proxyHost, proxyPort);
            params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

            String proxyUser = System.getProperty(JMeter.HTTP_PROXY_USER, JMeterUtils.getProperty(JMeter.HTTP_PROXY_USER));
            if (proxyUser != null) {
                log.info("Using authenticated proxy with username: " + proxyUser);
                String proxyPass = System.getProperty(JMeter.HTTP_PROXY_PASS, JMeterUtils.getProperty(JMeter.HTTP_PROXY_PASS));

                String localHost;
                try {
                    localHost = InetAddress.getLocalHost().getCanonicalHostName();
                } catch (Throwable e) {
                    log.error("Failed to get local host name, defaulting to 'localhost'", e);
                    localHost = "localhost";
                }

                AuthScope authscope = new AuthScope(proxyHost, proxyPort);
                String proxyDomain = JMeterUtils.getPropDefault("http.proxyDomain", "");
                NTCredentials credentials = new NTCredentials(proxyUser, proxyPass, localHost, proxyDomain);
                client.getCredentialsProvider().setCredentials(authscope, credentials);
            }
        }
        return client;
    }

    public AbstractHttpClient getHttpClient() {
        return httpClient;
    }

    public StatusNotifierCallback getNotifier() {
        return notifier;
    }

    public String getAddress() {
        return address;
    }

    public String getDataAddress() {
        return dataAddress;
    }
}
