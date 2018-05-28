/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.blazemeter.jmeter.http2.sampler;

import com.google.common.annotations.VisibleForTesting;
import com.thoughtworks.xstream.XStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jmeter.threads.SamplePackage;
import org.eclipse.jetty.http.HttpFields;

/**
 * This is a specialisation of the SampleResult class for the HTTP protocol.
 */
public class HTTP2SampleResult extends HTTPSampleResult {

  private static final long serialVersionUID = 241L;

  /**
   * Set of all HTTP methods, that have no body
   */
  private static final Set<String> METHODS_WITHOUT_BODY = new HashSet<>(
      Arrays.asList(HTTPConstants.HEAD, HTTPConstants.OPTIONS, HTTPConstants.TRACE));

  private static int idCount = 0;

  private int id;

  private int embebedResultsDepth;
  private HttpFields httpFieldsResponse;
  private boolean embebedResults;
  private transient Queue<HTTP2SampleResult> pendingResults = new ConcurrentLinkedQueue<>();

  private boolean secondaryRequest;

  private String embeddedUrlRE;

  private boolean isPushed;

  private transient ListenerNotifier listenerNotifier = new ListenerNotifier();

  private transient JMeterVariables threadVars;
  private transient SamplePackage pack;
  /**
   * The raw value of the Location: header; may be null. This is supposed to be an absolute URL: <a
   * href= "http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.30">RFC2616 sec14.30</a>
   * but is often relative.
   */
  private String redirectLocation;

  protected static final String NON_HTTP_RESPONSE_CODE = "Non HTTP response code";

  protected static final String NON_HTTP_RESPONSE_MESSAGE = "Non HTTP response message";
  protected static final String HTTP2_PENDING_RESPONSE = "Pending";
  protected static final String HTTP2_RESPONSE_RECEIVED = "Received";
  protected static final String HTTP2_RESPONSE_CODE_4 = "Not Found";
  private boolean pendingResponse;

  private String requestId;

  static {
    registerHTTP2ResultConverter();
  }

  public static void registerHTTP2ResultConverter (){
    try {
      Method method = SaveService.class
          .getDeclaredMethod("registerConverter", String.class, XStream.class, boolean.class);
      method.setAccessible(true);
      Field target = SaveService.class.getDeclaredField("JTLSAVER");
      target.setAccessible(true);
      XStream JTLSAVER = (XStream) target.get(null);
      method.invoke(null, HTTP2ResultConverter.class.getCanonicalName(), JTLSAVER, true);
    } catch (NoSuchMethodException | NoSuchFieldException | InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public HTTP2SampleResult() {
  }

  public HTTP2SampleResult(String sampleName, JMeterVariables threadVars,
      int nbActiveThreadsInThreadGroup, int nbTotalActiveThreads,
      String threadGroupName) {

    setGroupThreads(nbActiveThreadsInThreadGroup);
    setAllThreads(nbTotalActiveThreads);
    setThreadName(threadGroupName);
    this.threadVars = threadVars;
    this.pack = (SamplePackage) threadVars.getObject(JMeterThread.PACKAGE_OBJECT);

    setSampleLabel(sampleName);
    this.setPendingResponse(true);
    this.setId(HTTP2SampleResult.getNextId());
    this.setEmbebedResultsDepth(1);
    this.setResponseCode(HTTP2_PENDING_RESPONSE);
    this.setResponseMessage(HTTP2_PENDING_RESPONSE);
  }

  public HTTP2SampleResult(URL url, String method, JMeterVariables threadVars,
      int nbActiveThreadsInThreadGroup, int nbTotalActiveThreads,
      String threadGroupName) {
    this(url.toString(), threadVars, nbActiveThreadsInThreadGroup, nbTotalActiveThreads,
        threadGroupName);
    this.setHTTPMethod(method);
    this.setURL(url);
  }

  /**
   * Construct a 'parent' result for an already-existing result, essentially cloning it
   *
   * @param res existing sample result
   */
  public HTTP2SampleResult(HTTP2SampleResult res) {
    super(res);
    redirectLocation = res.redirectLocation;
  }

  protected static HTTP2SampleResult errorResult(String message) {
    HTTP2SampleResult res = new HTTP2SampleResult();
    res.setDataType(SampleResult.TEXT);
    res.setResponseData(message, null);
    res.setResponseCode(NON_HTTP_RESPONSE_CODE);
    res.setResponseMessage(message);
    res.setSuccessful(false);
    res.setPendingResponse(false);
    return res;
  }

  /**
   * Populates the provided HTTPSampleResult with details from the Exception. Does not create a new
   * instance, so should not be used directly to add a subsample.
   *
   * @param e Exception representing the error.
   * @param res SampleResult to be modified
   * @return the modified sampling result containing details of the Exception.
   */
  protected static void setResultError(HTTP2SampleResult res, Throwable e) {
    res.setDataType(SampleResult.TEXT);
    ByteArrayOutputStream text = new ByteArrayOutputStream(200);
    e.printStackTrace(new PrintStream(text));
    res.setResponseData(text.toByteArray());
    res.setResponseCode(NON_HTTP_RESPONSE_CODE + ": " + e.getClass().getName());
    res.setResponseMessage(NON_HTTP_RESPONSE_MESSAGE + ": " + e.getMessage());
    res.setSuccessful(false);
    res.setPendingResponse(false);
    // res.setMonitor(this.isMonitor()); // TODO see if applies to http2
  }

  public static synchronized int getNextId() {
    int ret = idCount;
    idCount += 1;
    return ret;
  }

  public boolean isEmbebedResults() {
    return embebedResults;
  }

  public String getEmbeddedUrlRE() {
    return embeddedUrlRE;
  }

  public void setEmbeddedUrlRE(String embeddedUrlRE) {
    this.embeddedUrlRE = embeddedUrlRE;
  }

  public void setEmbebedResults(boolean embebedResults) {
    this.embebedResults = embebedResults;
  }

  public int getEmbebedResultsDepth() {
    return embebedResultsDepth;
  }

  public void setEmbebedResultsDepth(int embebedResultsDepth) {
    this.embebedResultsDepth = embebedResultsDepth;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public boolean isPushed() {
    return isPushed;
  }

  public void setPushed(boolean pushed) {
    isPushed = pushed;
  }

  public HttpFields getHttpFieldsResponse() {
    return httpFieldsResponse;
  }

  public void setHttpFieldsResponse(HttpFields httpFieldsResponse) {
    this.httpFieldsResponse = httpFieldsResponse;
  }

  public void setRedirectLocation(String redirectLocation) {
    this.redirectLocation = redirectLocation;
  }

  public String getRedirectLocation() {
    return redirectLocation;
  }

  /**
   * Determine whether this result is a redirect. Returns true for: 301,302,303 and 307(GET or
   * HEAD)
   *
   * @return true iff res is an HTTP redirect response
   */
  public boolean isRedirect() {
    /*
     * Don't redirect the following: 300 = Multiple choice 304 = Not
     * Modified 305 = Use Proxy 306 = (Unused)
     */
    final String[] REDIRECT_CODES = {HTTPConstants.SC_MOVED_PERMANENTLY,
        HTTPConstants.SC_MOVED_TEMPORARILY,
        HTTPConstants.SC_SEE_OTHER};
    String code = getResponseCode();
    for (String redirectCode : REDIRECT_CODES) {
      if (redirectCode.equals(code)) {
        return true;
      }
    }
    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
    // If the 307 status code is received in response to a request other
    // than GET or HEAD,
    // the user agent MUST NOT automatically redirect the request unless it
    // can be confirmed by the user,
    // since this might change the conditions under which the request was
    // issued.
    // See Bug 54119
    if (HTTPConstants.SC_TEMPORARY_REDIRECT.equals(code)
        && (HTTPConstants.GET.equals(getHTTPMethod()) || HTTPConstants.HEAD
        .equals(getHTTPMethod()))) {
      return true;
    }
    return false;
  }

  /**
   * Overrides version in Sampler data to provide more details
   * <p>
   * {@inheritDoc}
   */


  /**
   * Overrides the method from SampleResult - so the encoding can be extracted from the Meta
   * content-type if necessary.
   * <p>
   * Updates the dataEncoding field if the content-type is found.
   *
   * @param defaultEncoding Default encoding used if there is no data encoding
   * @return the dataEncoding value as a String
   */
  @Override
  public String getDataEncodingWithDefault(String defaultEncoding) {
    String dataEncodingNoDefault = getDataEncodingNoDefault();
    if (dataEncodingNoDefault != null && dataEncodingNoDefault.length() > 0) {
      return dataEncodingNoDefault;
    }
    return defaultEncoding;
  }

  /**
   * Overrides the method from SampleResult - so the encoding can be extracted from the Meta
   * content-type if necessary.
   * <p>
   * Updates the dataEncoding field if the content-type is found.
   *
   * @return the dataEncoding value as a String
   */
  @Override
  public String getDataEncodingNoDefault() {
    if (super.getDataEncodingNoDefault() == null && getContentType()
        .startsWith("text/html")) { // $NON-NLS-1$
      byte[] bytes = getResponseData();
      // get the start of the file
      String prefix = new String(bytes, 0, Math.min(bytes.length, 2000),
          Charset.forName(DEFAULT_HTTP_ENCODING));
      // Preserve original case
      String matchAgainst = prefix.toLowerCase(java.util.Locale.ENGLISH);
      // Extract the content-type if present
      final String METATAG = "<meta http-equiv=\"content-type\" content=\""; // $NON-NLS-1$
      int tagstart = matchAgainst.indexOf(METATAG);
      if (tagstart != -1) {
        tagstart += METATAG.length();
        int tagend = prefix.indexOf('\"', tagstart); // $NON-NLS-1$
        if (tagend != -1) {
          final String ct = prefix.substring(tagstart, tagend);
          setEncodingAndType(ct);// Update the dataEncoding
        }
      }
    }
    return super.getDataEncodingNoDefault();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.jmeter.samplers.SampleResult#getSearchableTokens()
   */
  @Override
  public List<String> getSearchableTokens() throws Exception {
    List<String> list = new ArrayList<>(super.getSearchableTokens());
    list.add(getQueryString());
    list.add(getCookies());
    return list;
  }

  public void setPendingResponse(boolean pendingResp) {
    pendingResponse = pendingResp;
  }

  public boolean isPendingResponse() {
    return pendingResponse;
  }

  public void setRequestId(String id) {
    requestId = id;
  }

  public String getRequestId() {
    return requestId;
  }


  public boolean isSecondaryRequest() {
    return secondaryRequest;
  }

  public void setSecondaryRequest(boolean secondaryRequest) {
    this.secondaryRequest = secondaryRequest;
  }

  public void addPendingResult(HTTP2SampleResult pendingResult) {
    this.pendingResults.add(pendingResult);
  }

  public JMeterVariables getThreadVars (){
    return threadVars;
  }

  public Queue<HTTP2SampleResult> getPendingResults() {
    return pendingResults;
  }

  public void notifySample() {
    HTTP2SampleResult parent = (HTTP2SampleResult) this.getParent();
    if (parent != null) {
      parent.notifySample();
    }
    else if (!isPendingResponse()) {
      boolean sonsArePendingResponse = false;
      for (SampleResult s : getSubResults()) {
        HTTP2SampleResult h = (HTTP2SampleResult) s;
        if (h.isPendingResponse()) {
          sonsArePendingResponse = true;
          break;
        }
      }
      if (!sonsArePendingResponse) {
        SampleEvent event = new SampleEvent(this, getThreadName(),
            threadVars, false);
        listenerNotifier.notifyListeners(event, pack.getSampleListeners());
      }
    }
  }

  @VisibleForTesting
  public void setListenerNotifier(ListenerNotifier listenerNotifier) {
    this.listenerNotifier = listenerNotifier;
  }
}
