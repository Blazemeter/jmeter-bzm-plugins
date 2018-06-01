package com.blazemeter.jmeter.http2.sampler;

import com.google.common.annotations.VisibleForTesting;
import com.thoughtworks.xstream.XStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jmeter.threads.SamplePackage;
import org.eclipse.jetty.http.HttpFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTP2SampleResult extends HTTPSampleResult {

  private static final String HTTP2_PENDING_RESPONSE = "Pending";
  private int embebedResultsDepth;
  private HttpFields httpFieldsResponse;
  private boolean embebedResults;
  private boolean secondaryRequest;
  private String embeddedUrlRE;
  private transient ListenerNotifier listenerNotifier = new ListenerNotifier();
  private transient JMeterVariables threadVars;
  private transient SamplePackage pack;
  private String redirectLocation;
  private boolean pendingResponse;

  private static final Logger LOG = LoggerFactory.getLogger(HTTP2SampleResult.class);


  static {
    registerHTTP2ResultConverter();
  }

  private static void registerHTTP2ResultConverter() {
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

  public HTTP2SampleResult(HTTP2SampleResult res) {
    super(res);
    redirectLocation = res.redirectLocation;
  }

  protected void setErrorResult(String message, Throwable e) {
    setDataType(HTTPSampleResult.TEXT);
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    setResponseData(sw.toString(), HTTPSampleResult.DEFAULT_HTTP_ENCODING);
    setResponseCode(e.getClass().getName());
    setResponseMessage(e.getMessage());
    setSuccessful(false);
    setPendingResponse(false);
    LOG.error(message, e);
  }

  public static HTTP2SampleResult createErrorResult(String message, Throwable e) {
    HTTP2SampleResult result = new HTTP2SampleResult();
    result.setErrorResult(message, e);
    return result;
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


  public void setPendingResponse(boolean pendingResp) {
    pendingResponse = pendingResp;
  }

  public boolean isPendingResponse() {
    return pendingResponse;
  }

  public boolean isSecondaryRequest() {
    return secondaryRequest;
  }

  public void setSecondaryRequest(boolean secondaryRequest) {
    this.secondaryRequest = secondaryRequest;
  }

  public JMeterVariables getThreadVars() {
    return threadVars;
  }

  public void notifySample() {
    HTTP2SampleResult parent = (HTTP2SampleResult) this.getParent();
    if (parent != null) {
      parent.notifySample();
    } else if (!isPendingResponse()) {
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
