package com.blazemeter.jmeter.http2.sampler;

import com.google.common.annotations.VisibleForTesting;
import com.thoughtworks.xstream.XStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.AbstractScopedAssertion;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jmeter.threads.SamplePackage;
import org.apache.jorphan.util.JMeterError;
import org.eclipse.jetty.http.HttpFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTP2SampleResult extends HTTPSampleResult {

  private int embebedResultsDepth;
  private boolean embebedResults;
  private String embeddedUrlRE;
  private boolean secondaryRequest;
  private boolean pendingResponse;
  private transient HttpFields httpFieldsResponse;
  private transient ListenerNotifier listenerNotifier = new ListenerNotifier();
  private transient JMeterContext threadContext;
  private transient JMeterVariables threadVars;
  private transient List<SampleListener> sampleListeners;
  private transient List<Assertion> assertions;
  private transient List<PostProcessor> postProcessors;

  private transient boolean isSync;

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
    } catch (ReflectiveOperationException e) {
      LOG.error("This version of JMeter is not supported", e);
    }
  }

  public HTTP2SampleResult() {
  }

  public HTTP2SampleResult(JMeterContext threadContext) {
    this.threadContext = threadContext;
    this.threadVars = threadContext.getVariables();
    SamplePackage pack = (SamplePackage) threadVars.getObject(JMeterThread.PACKAGE_OBJECT);
    this.assertions = pack.getAssertions();
    this.postProcessors = pack.getPostProcessors();
    this.sampleListeners = pack.getSampleListeners();
    this.setPendingResponse(true);
    this.setEmbebedResultsDepth(1);
  }

  public HTTP2SampleResult(HTTP2SampleResult res) {
    super(res);
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

  public HTTP2SampleResult createSubResult() {
    HTTP2SampleResult result = new HTTP2SampleResult();
    result.embebedResultsDepth = this.embebedResultsDepth - 1;
    result.embebedResults = this.embebedResults;
    result.secondaryRequest = true;
    result.pendingResponse = true;
    result.threadContext = this.threadContext;
    result.threadVars = threadVars;
    result.sampleListeners = null;
    result.assertions = null;
    result.postProcessors = null;
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

  public boolean isSync() {
    return isSync;
  }

  public void setSync(boolean sync) {
    isSync = sync;
  }

  public void completeAsyncSample() {
    HTTP2SampleResult parent = (HTTP2SampleResult) this.getParent();
    if (parent != null) {
      parent.completeAsyncSample();
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
        runPostProcessors(postProcessors);
        checkAssertions(assertions);
        SampleEvent event = new SampleEvent(this, getThreadName(),
            threadVars, false);
        listenerNotifier.notifyListeners(event, this.sampleListeners);
      }
    }
  }

  private void runPostProcessors(List<PostProcessor> postProcessors) {
    for (PostProcessor postProcessor : postProcessors) {
      if (threadContext.getVariables() != null) {
        TestBeanHelper.prepare((TestElement) postProcessor);
        postProcessor.process();
      } else {
        LOG.warn(
            "The Post Processor " + postProcessor.getClass() + "was not executed for the sampler"
                + getSampleLabel() + ". Use Synchronized Request to avoid this error");
      }
    }
  }

  private void checkAssertions(List<Assertion> assertions) {
    for (Assertion assertion : assertions) {
      TestBeanHelper.prepare((TestElement) assertion);
      if (assertion instanceof AbstractScopedAssertion) {
        AbstractScopedAssertion scopedAssertion = (AbstractScopedAssertion) assertion;
        String scope = scopedAssertion.fetchScope();
        if (scopedAssertion.isScopeParent(scope)
            || scopedAssertion.isScopeAll(scope)) {
          processAssertion(this, assertion);
        }
        if (scopedAssertion.isScopeVariable(scope)) {
          if (((AbstractScopedAssertion) assertion).getThreadContext().getVariables() != null) {
            processAssertion(this, assertion);
          } else {
            LOG.warn("The Variable Assertion was not executed for the sampler" + getSampleLabel()
                + ". Use Synchronized Request to avoid this error");
          }
        }
        if (scopedAssertion.isScopeChildren(scope)
            || scopedAssertion.isScopeAll(scope)) {
          SampleResult[] children = this.getSubResults();
          boolean childError = false;
          for (SampleResult childSampleResult : children) {
            processAssertion(childSampleResult, assertion);
            if (!childSampleResult.isSuccessful()) {
              childError = true;
            }
          }
          // If sampleResult is OK, but child failed, add a message and flag the sampleResult as failed
          if (childError && this.isSuccessful()) {
            AssertionResult assertionResult = new AssertionResult(
                ((AbstractTestElement) assertion).getName());
            assertionResult.setResultForFailure("One or more sub-samples failed");
            this.addAssertionResult(assertionResult);
            this.setSuccessful(false);
          }
        }
      } else {
        processAssertion(this, assertion);
      }
    }
    threadVars.put("JMeterThread.last_sample_ok", Boolean.toString(this.isSuccessful()));
  }

  private void processAssertion(SampleResult result, Assertion assertion) {
    AssertionResult assertionResult;
    try {
      assertionResult = assertion.getResult(result);
    } catch (AssertionError e) {
      LOG.debug("Error processing Assertion.", e);
      assertionResult = new AssertionResult("Assertion failed! See log file (debug level, only).");
      assertionResult.setFailure(true);
      assertionResult.setFailureMessage(e.toString());
    } catch (JMeterError e) {
      LOG.error("Error processing Assertion.", e);
      assertionResult = new AssertionResult("Assertion failed! See log file.");
      assertionResult.setError(true);
      assertionResult.setFailureMessage(e.toString());
    } catch (Exception e) {
      LOG.error("Exception processing Assertion.", e);
      assertionResult = new AssertionResult("Assertion failed! See log file.");
      assertionResult.setError(true);
      assertionResult.setFailureMessage(e.toString());
    }
    result.setSuccessful(
        result.isSuccessful() && !(assertionResult.isError() || assertionResult.isFailure()));
    result.addAssertionResult(assertionResult);
  }

  @VisibleForTesting
  public void setListenerNotifier(ListenerNotifier listenerNotifier) {
    this.listenerNotifier = listenerNotifier;
  }
}
