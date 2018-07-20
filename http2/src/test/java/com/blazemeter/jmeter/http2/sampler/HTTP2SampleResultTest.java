package com.blazemeter.jmeter.http2.sampler;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.threads.SamplePackage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HTTP2SampleResultTest {

  @Mock
  private JMeterVariables threadVars;

  @Mock
  private transient JMeterContext threadContext;

  @Mock
  private SamplePackage pack;

  @Before
  public void setup() {
    List<SampleListener> sampleListeners = new ArrayList<>();
    List<Assertion> assertions = new ArrayList<>();
    List<PostProcessor> postProcessors = new ArrayList<>();
    TestJMeterUtils.createJmeterEnv();
    when(threadContext.getVariables()).thenReturn(threadVars);
    when(threadVars.getObject(any(String.class))).thenReturn(pack);
    when(pack.getAssertions()).thenReturn(assertions);
    when(pack.getPostProcessors()).thenReturn(postProcessors);
    when(pack.getSampleListeners()).thenReturn(sampleListeners );
  }

  @Test
  public void shouldSetCorrectlyTheErrorResult() throws MalformedURLException {
    HTTP2SampleResult sampleRes = new HTTP2SampleResult(threadContext);
    Throwable e = new Throwable();
    sampleRes.setErrorResult("message", e);
    HTTP2SampleResult expected = new HTTP2SampleResult(threadContext);
    expected.setDataType(HTTPSampleResult.TEXT);
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    expected.setResponseData(sw.toString(), HTTPSampleResult.DEFAULT_HTTP_ENCODING);
    expected.setResponseCode(e.getClass().getName());
    expected.setResponseMessage(e.getMessage());
    expected.setSuccessful(false);
    expected.setPendingResponse(false);
    assertThat(sampleRes)
        .isEqualToComparingOnlyGivenFields(expected, "dataType", "responseDataAsString",
            "responseCode",
            "responseMessage", "success", "pendingResponse");
  }

}
