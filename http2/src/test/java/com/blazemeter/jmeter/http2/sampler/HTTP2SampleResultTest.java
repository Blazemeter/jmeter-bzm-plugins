package com.blazemeter.jmeter.http2.sampler;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.threads.SamplePackage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HTTP2SampleResultTest {

  @Mock
  private JMeterVariables threadVars;

  @Mock
  private SamplePackage pack;

  @Before
  public void setup() {
    TestJMeterUtils.createJmeterEnv();
    Mockito.when(threadVars.getObject(Mockito.any(String.class))).thenReturn(pack);
  }

  @Test
  public void shouldSetCorrectlyTheErrorResult() throws MalformedURLException {
    URL url = new URL("https", "www.sprint.com", 443, "/");
    HTTP2SampleResult sampleRes = new HTTP2SampleResult(url, "GET", threadVars, 0, 0, "");
    Throwable e = new Throwable();
    sampleRes.setErrorResult("message", e);
    HTTP2SampleResult expected = new HTTP2SampleResult(url, "GET", threadVars, 0, 0, "");
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
