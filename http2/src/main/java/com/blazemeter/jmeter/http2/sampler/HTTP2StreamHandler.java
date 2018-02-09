
package com.blazemeter.jmeter.http2.sampler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.parser.BaseParser;
import org.apache.jmeter.protocol.http.parser.LinkExtractorParseException;
import org.apache.jmeter.protocol.http.parser.LinkExtractorParser;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.util.ConversionUtils;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;
import org.apache.oro.text.MalformedCachePatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Matcher;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.api.Stream.Listener;
import org.eclipse.jetty.http2.frames.DataFrame;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.http2.frames.PushPromiseFrame;
import org.eclipse.jetty.util.Callback;

public class HTTP2StreamHandler extends Stream.Listener.Adapter {

    private static final String USER_AGENT = "User-Agent";
    private static final Map<String, String> PARSERS_FOR_CONTENT_TYPE = new HashMap<>();

    private static final String RESPONSE_PARSERS = // list of parsers
            JMeterUtils.getProperty("HTTPResponse.parsers");//$NON-NLS-1$

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final boolean IGNORE_FAILED_EMBEDDED_RESOURCES = JMeterUtils
            .getPropDefault("httpsampler.ignore_failed_embedded_resources", false); // $NON-NLS-1$

    static {
        String[] parsers = JOrphanUtils.split(RESPONSE_PARSERS, " ", true);
        for (final String parser : parsers) {
            String classname = JMeterUtils.getProperty(parser + ".className");//$NON-NLS-1$
            if (classname == null) {
                log.error("Cannot find .className property for " + parser + ", ensure you set property:'" + parser
                        + ".className'");
                continue;
            }
            String typeList = JMeterUtils.getProperty(parser + ".types");//$NON-NLS-1$
            if (typeList != null) {
                String[] types = JOrphanUtils.split(typeList, " ", true);
                for (final String type : types) {
                    log.info("Parser for " + type + " is " + classname);
                    PARSERS_FOR_CONTENT_TYPE.put(type, classname);
                }
            } else {
                log.warn("Cannot find .types property for " + parser
                        + ", as a consequence parser will not be used, to make it usable, define property:'" + parser
                        + ".types'");
            }
        }
    }

    private final CompletableFuture<Void> completedFuture = new CompletableFuture<>();
    private HTTP2SampleResult result;
    private HTTP2Connection parent;
    private URL url;
    private byte[] responseBytes;
    private HeaderManager headerManager;
    private CookieManager cookieManager;
    private boolean first = true;
    private int timeout = 0;

    public HTTP2StreamHandler(HTTP2Connection parent, URL url, HeaderManager headerManager, CookieManager cookieManager, HTTP2SampleResult sampleResult) {
        this.result = sampleResult;
        this.parent = parent;
        this.url = url;
        this.cookieManager = cookieManager;
        this.headerManager = headerManager;
    }

    public CompletableFuture<Void> getCompletedFuture() {
        return completedFuture;
    }

    @Override
    public Listener onPush(Stream stream, PushPromiseFrame frame) {
        HTTP2SampleResult sampleResult = new HTTP2SampleResult(url, "PUSHED FROM " + frame.getStreamId());
        sampleResult.setRequestHeaders(frame.toString());
        sampleResult.setPushed(true);
        sampleResult.setEmbebedResults(false);
        HTTP2StreamHandler hTTP2StreamHandler = new HTTP2StreamHandler(this.parent, null, headerManager, cookieManager, sampleResult);
        this.parent.addStreamHandler(hTTP2StreamHandler);
        return hTTP2StreamHandler;
    }

    @Override
    public void onHeaders(Stream stream, HeadersFrame frame) {

        MetaData.Response responseMetadata = ((MetaData.Response) frame.getMetaData());
        // set status line - header[0] is not the status line...
        StringBuilder header = new StringBuilder(responseMetadata.getHttpVersion() + " " + Integer.toString(responseMetadata.getStatus()) + "\n");
        result.setResponseCode(Integer.toString(responseMetadata.getStatus()));
        HttpFields hdrs = new HttpFields();
        for (HttpField h : frame.getMetaData().getFields()) {
            header.append(h.getName()).append(": ").append(h.getValue()).append("\n");
            switch (h.getName()) {
                case HTTPConstants.HEADER_CONTENT_TYPE:// TODO adapt to translate gzip, etc
                case "content-type":
                    result.setContentType(h.getValue());
                    result.setEncodingAndType(h.getValue());
                    break;
                case HTTPConstants.HEADER_CONTENT_ENCODING:
                    result.setDataEncoding(h.getValue());
                    break;
            }
            hdrs.add(h);
        }
        header.append("\n");
        result.setResponseHeaders(header.toString());
        result.setHeadersSize(header.length());
        result.setBytes(result.getBytesAsLong() + result.getHeadersSize());
        result.setHttpFieldsResponse(hdrs);
        if (frame.isEndStream()) {
            result.sampleEnd();
            result.setPendingResponse(false);
            completedFuture.complete(null);

        }
        if (!result.isPendingResponse()) {
            // the sample has failed for another reason (eg. timeout)
            result.setResponseHeaders("");
        }
    }

    @Override
    public void onData(Stream stream, DataFrame frame, Callback callback) {
        callback.succeeded();
        byte[] bytes = new byte[frame.getData().remaining()];
        frame.getData().get(bytes);

        try {
            if (first) {
                result.latencyEnd();
                first = false;
            }
            setResponseBytes(bytes);

            if (frame.isEndStream() && result.isPendingResponse()) {
                // finished the response and the sample has not failed for
                // another reason (eg. timeout)
                result.sampleEnd();
                if (!result.isPushed()) {

                    // Now collect the results into the HTTP2SampleResult:
                    // TODO Collect connect time and sent bytes
                    int responseLevel = Integer.parseInt(result.getResponseCode()) / 100;
                    switch (responseLevel) {
                        case 3:
                            break;
                        case 4:
                            result.setResponseMessage(HTTP2SampleResult.HTTP2_RESPONSE_CODE_4);
                            // TODO message depends on the code number
                            break;
                        case 5:
                            break;
                        default:
                            result.setResponseMessage(HTTP2SampleResult.HTTP2_RESPONSE_RECEIVED);
                            break;
                    }

                    result.setSuccessful(isSuccessCode(Integer.parseInt(result.getResponseCode())));
                    result.setResponseData(this.responseBytes);
                    result.setPendingResponse(false);
                    result.setBodySize((long) this.responseBytes.length);
                    result.setBytes(result.getBytesAsLong() + result.getBodySizeAsLong());

                    if (result.isRedirect()) {
                        // TODO redirect
                    }

                    if ((result.isEmbebedResults()) && (result.getEmbebedResultsDepth() > 0)
                            && (result.getDataType().equals(SampleResult.TEXT))) {
                        getPageResources(result);
                    }

                    if (result.isSecondaryRequest()) {
                        HTTP2SampleResult parent = (HTTP2SampleResult) result.getParent();
                        // set primary request failed if at least one secondary
                        // request fail
                        setParentSampleSuccess(parent,
                                parent.isSuccessful() && (result == null || result.isSuccessful()));
                    }
                    completedFuture.complete(null);

                } else {
                    // TODO support push
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // TODO
        }

    }

    /**
     * Set parent successful attribute based on IGNORE_FAILED_EMBEDDED_RESOURCES
     * parameter
     *
     * @param res          {@link HTTP2SampleResult}
     * @param initialValue boolean
     */
    private void setParentSampleSuccess(HTTP2SampleResult res, boolean initialValue) {
        if (!IGNORE_FAILED_EMBEDDED_RESOURCES) {
            res.setSuccessful(initialValue);
            if (!initialValue) {
                StringBuilder detailedMessage = new StringBuilder(80);
                detailedMessage.append("Embedded resource download error:"); //$NON-NLS-1$
                for (SampleResult subResult : res.getSubResults()) {
                    HTTP2SampleResult httpSampleResult = (HTTP2SampleResult) subResult;
                    if (!httpSampleResult.isSuccessful()) {
                        detailedMessage.append(httpSampleResult.getURL()).append(" code:") //$NON-NLS-1$
                                .append(httpSampleResult.getResponseCode()).append(" message:") //$NON-NLS-1$
                                .append(httpSampleResult.getResponseMessage()).append(", "); //$NON-NLS-1$
                    }
                }
                res.setResponseMessage(detailedMessage.toString()); // $NON-NLS-1$
            }
        }
    }

    private void setResponseBytes(byte[] bytes) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            if (this.responseBytes != null) {
                outputStream.write(this.responseBytes);
            }
            outputStream.write(bytes);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.responseBytes = outputStream.toByteArray();
    }

    /**
     * @param url URL to escape
     * @return escaped url
     */
    private URL escapeIllegalURLCharacters(java.net.URL url) {
        if (url == null || url.getProtocol().equals("file")) {
            return url;
        }
        try {
            return ConversionUtils.sanitizeUrl(url).toURL();
        } catch (Exception e1) {
            //TODO fix log
            return url;
        }
    }

    /**
     * Download the resources of an HTML page.
     *
     * @param res        result of the initial request - must contain an HTML response
     *                   and for storing the results, if any
     */
    private void getPageResources(HTTP2SampleResult res) throws Exception {
        Iterator<URL> urls = null;
        try {
            final byte[] responseData = res.getResponseData();
            if (responseData.length > 0) { // Bug 39205
                final LinkExtractorParser parser = getParser(res);
                if (parser != null) {
                    String userAgent = getUserAgent(res);
                    String encoding = res.getDataEncodingWithDefault();
                    urls = parser.getEmbeddedResourceURLs(userAgent, responseData, res.getURL(), encoding);
                }
            }
        } catch (LinkExtractorParseException e) {
            // Don't break the world just because this failed:
            System.out.println(e);
            res.addSubResult(HTTP2SampleResult.errorResult(e, new HTTP2SampleResult(res)));
            setParentSampleSuccess(res, false);
        }

        // Iterate through the URLs and download each image:
        if (urls != null && urls.hasNext()) {
            // Get the URL matcher
            String re = res.getEmbeddedUrlRE();
            Perl5Matcher localMatcher = null;
            Pattern pattern = null;
            if (re.length() > 0) {
                try {
                    pattern = JMeterUtils.getPattern(re);
                    localMatcher = JMeterUtils.getMatcher();// don't fetch unless pattern compiles
                } catch (MalformedCachePatternException e) {
                    //TODO Log
                }
            }

            while (urls.hasNext()) {
                Object binURL = urls.next(); // See catch clause below
                try {
                    URL url = (URL) binURL;
                    if (url == null) {
                        //TODO Log
                    } else {
                        try {
                            url = escapeIllegalURLCharacters(url);
                        } catch (Exception e) {
                            res.addSubResult(HTTP2SampleResult.errorResult(
                                    new Exception(url.toString() + " is not a correct URI"),
                                    new HTTP2SampleResult(res)));
                            setParentSampleSuccess(res, false);
                            continue;
                        }
                        // I don't think localMatcher can be null here, but
                        // check just in case
                        if (pattern != null && localMatcher != null && !localMatcher.matches(url.toString(), pattern)) {
                            continue; // we have a pattern and the URL does not match, so skip it
                        }
                        try {
                            url = url.toURI().normalize().toURL();
                        } catch (MalformedURLException | URISyntaxException e) {
                            res.addSubResult(HTTP2SampleResult.errorResult(
                                    new Exception(url.toString() + " URI can not be normalized", e),
                                    new HTTP2SampleResult(res)));
                            setParentSampleSuccess(res, false);
                            continue;
                        }

                        HTTP2SampleResult subResult = new HTTP2SampleResult(url, "GET");
                        subResult.setSecondaryRequest(true);
                        subResult.setEmbebedResultsDepth(res.getEmbebedResultsDepth() - 1);
                        res.addSubResult(subResult);

                        parent.send("GET", url, headerManager, cookieManager, null, subResult, this.timeout);

                    }
                } catch (ClassCastException e) { // TODO can this happen?
                    res.addSubResult(HTTP2SampleResult.errorResult(new Exception(binURL + " is not a correct URI"),
                            new HTTP2SampleResult(res)));
                    setParentSampleSuccess(res, false);
                }
            }

        }
    }

    /**
     * Gets parser from {@link HTTPSampleResult#getMediaType()}. Returns null if
     * no parser defined for it
     *
     * @param res {@link HTTPSampleResult}
     * @return {@link LinkExtractorParser}
     * @throws LinkExtractorParseException
     */
    private LinkExtractorParser getParser(HTTP2SampleResult res) throws LinkExtractorParseException {
        String parserClassName = PARSERS_FOR_CONTENT_TYPE.get(res.getMediaType());
        if (!StringUtils.isEmpty(parserClassName)) {
            return BaseParser.getParser(parserClassName);
        }
        return null;
    }

    private String getUserAgent(HTTP2SampleResult sampleResult) {
        String res = sampleResult.getRequestHeaders();
        int index = res.indexOf(USER_AGENT);
        if (index >= 0) {
            // see HTTPHC3Impl#getConnectionHeaders
            // see HTTPHC4Impl#getConnectionHeaders
            // see HTTPJavaImpl#getConnectionHeaders
            // ': ' is used by JMeter to fill-in requestHeaders, see
            // getConnectionHeaders
            final String userAgentPrefix = USER_AGENT + ": ";
            String userAgentHdr = res.substring(index + userAgentPrefix.length(), res.indexOf('\n',
                    // '\n' is used by JMeter to fill-in requestHeaders, see getConnectionHeaders
                    index + userAgentPrefix.length() + 1));
            return userAgentHdr.trim();
        } else {
            //TODO log
            return null;
        }
    }

    /**
     * Determine if the HTTP status code is successful or not i.e. in range 200
     * to 399 inclusive
     *
     * @param code status code to check
     * @return whether in range 200-399 or not
     */
    protected boolean isSuccessCode(int code) {
        return code >= 200 && code <= 399;
    }

    protected HTTP2SampleResult getHTTP2SampleResult() {
        return this.result;
    }

    protected int getTimeout() {
        return timeout;
    }

    protected void setTimeout(int timeout) {
        this.timeout = timeout;
    }

}
