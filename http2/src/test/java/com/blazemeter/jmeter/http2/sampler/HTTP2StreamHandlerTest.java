package com.blazemeter.jmeter.http2.sampler;

import static org.eclipse.jetty.http.MetaData.Request;
import static org.eclipse.jetty.http.MetaData.Response;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jmeter.threads.SamplePackage;
import org.apache.jmeter.util.JMeterUtils;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http2.ErrorCode;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.frames.DataFrame;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.http2.frames.PushPromiseFrame;
import org.eclipse.jetty.http2.frames.ResetFrame;
import org.eclipse.jetty.util.Callback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class HTTP2StreamHandlerTest {

  private HTTP2StreamHandler http2StreamHandler;
  private HTTP2Connection http2Connection;
  private URL url;
  private HTTP2SampleResult http2SampleResult;
  private Stream stream;
  private PushPromiseFrame pushPromisFrame;
  private HeadersFrame headersFrame;
  private Response responseMetadata;
  private Callback callback;
  private DataFrame dataFrame;
  private JMeterVariables threadVars;
  private ListenerNotifier listener;
  private SamplePackage pack;
  private Request mockRequestMetadata;
  private HttpURI mockHttpURI;

  @Before
  public void setup() throws MalformedURLException {
    TestJMeterUtils.createJmeterEnv();
    JMeterUtils.setProperty("HTTPResponse.parsers", "htmlParser");
    JMeterUtils.setProperty("htmlParser.className",
        "org.apache.jmeter.protocol.http.parser.LagartoBasedHtmlParser");
    JMeterUtils.setProperty("htmlParser.types",
        "text/html application/xhtml+xml application/xml text/xml");
    url = new URL("http://www.tenfieldigital.com.uy");
    http2Connection = Mockito.mock(HTTP2Connection.class);
    stream = Mockito.mock(Stream.class);
    pushPromisFrame = Mockito.mock(PushPromiseFrame.class);
    headersFrame = Mockito.mock(HeadersFrame.class);
    responseMetadata = Mockito.mock(Response.class);
    callback = Mockito.mock(Callback.class);
    dataFrame = Mockito.mock(DataFrame.class);
    threadVars = Mockito.mock(JMeterVariables.class);
    listener = Mockito.mock(ListenerNotifier.class);
    pack = Mockito.mock(SamplePackage.class);
    mockRequestMetadata = Mockito.mock(Request.class);
    mockHttpURI = Mockito.mock(HttpURI.class);
  }

  @Test
  public void onPushTest() throws URISyntaxException, MalformedURLException {

    Mockito.when(pushPromisFrame.getStreamId()).thenReturn(5);
    Mockito.when(pushPromisFrame.toString()).thenReturn("TestString");
    Mockito.when(pushPromisFrame.getMetaData()).thenReturn(mockRequestMetadata);
    Mockito.when(mockRequestMetadata.getURI()).thenReturn(mockHttpURI);
    URI uri = new URI("http://www.test.com");
    Mockito.when(mockHttpURI.toURI()).thenReturn(uri);
    HttpFields httpFields = new HttpFields();
    httpFields.add("name1","value1");
    httpFields.add("name2","value2");
    Mockito.when(mockRequestMetadata.getFields()).thenReturn(httpFields);
    Mockito.when(mockRequestMetadata.getMethod()).thenReturn("GET");

    http2SampleResult = buildThreadVarsResult();

    http2SampleResult.setThreadName("");
    http2StreamHandler = new HTTP2StreamHandler(http2Connection, url, null, null,
        http2SampleResult);

    HTTP2StreamHandler res = (HTTP2StreamHandler) http2StreamHandler
        .onPush(stream, pushPromisFrame);

    HTTP2SampleResult resSR = res.getHTTP2SampleResult();

    assertEquals(uri.toURL(), resSR.getURL());
    assertEquals(uri.toURL().toString(), resSR.getSampleLabel());
    assertEquals("PUSHED FROM " + 5 + " GET", resSR.getHTTPMethod());
    assertEquals(1, resSR.getEmbebedResultsDepth());
    assertEquals("Pending", resSR.getResponseCode());
    assertEquals("Pending", resSR.getResponseMessage());
    assertEquals("name1: value1\nname2: value2\n\n", resSR.getRequestHeaders());
  }

  @Test
  public void onHeadersTest() {

    Mockito.when(headersFrame.getMetaData())
        .thenReturn(responseMetadata);

    Mockito.when(headersFrame.isEndStream())
        .thenReturn(true);

    Mockito.when(responseMetadata.getStatus())
        .thenReturn(200);

    Mockito.when(responseMetadata.getHttpVersion())
        .thenReturn(HttpVersion.HTTP_2);

    Mockito.when(pack.getSampleListeners()).thenReturn(null);

    HttpFields httpFields = new HttpFields();
    httpFields.add("Header1", "value1");
    httpFields.add("Header2", "value2");
    httpFields.add("Header3", "value3");
    httpFields.add("content-type", "application/json");
    httpFields.add(HTTPConstants.HEADER_CONTENT_ENCODING, "UTF-8");

    Mockito.when(responseMetadata.getFields())
        .thenReturn(httpFields);

    String headers =
        "HTTP/2.0 200 OK\nHeader1: value1\nHeader2: value2\nHeader3: value3\ncontent-type: application/json\n"
            + HTTPConstants.HEADER_CONTENT_ENCODING + ": UTF-8\n";

    http2SampleResult = buildThreadVarsResult();

    http2SampleResult.setThreadName("");
    http2StreamHandler = new HTTP2StreamHandler(http2Connection, url, null, null,
        http2SampleResult);

    HTTP2SampleResult resSR = http2StreamHandler.getHTTP2SampleResult();
    resSR.sampleStart();
    resSR.setListenerNotifier(listener);

    http2StreamHandler.onHeaders(stream, headersFrame);

    resSR = http2StreamHandler.getHTTP2SampleResult();

    assertEquals("200", resSR.getResponseCode());
    assertEquals("application/json", resSR.getContentType());
    assertEquals("UTF-8", resSR.getDataEncodingNoDefault());
    assertEquals(httpFields, resSR.getHttpFieldsResponse());
    assertEquals(headers, resSR.getResponseHeaders());
  }


  @Test
  public void onDataTest() {

    Mockito.when(dataFrame.getData())
        .thenReturn(ByteBuffer.allocate(1024 * 15));

    Mockito.when(dataFrame.isEndStream())
        .thenReturn(true);

    http2SampleResult = buildThreadVarsResult();

    http2StreamHandler = new HTTP2StreamHandler(http2Connection, url, null, null,
        http2SampleResult);

    HTTP2SampleResult resSR = http2StreamHandler.getHTTP2SampleResult();
    resSR.sampleStart();
    resSR.setPendingResponse(true);
    resSR.setResponseCode("200");
    resSR.setEmbebedResults(false);
    resSR.setSecondaryRequest(false);

    http2StreamHandler.onData(stream, dataFrame, callback);

    resSR = http2StreamHandler.getHTTP2SampleResult();

    assertTrue(resSR.isSuccessful());
    assertFalse(resSR.isPendingResponse());

  }

  @Test
  public void onDataTest2() {

    String data = "<html lang=\"es\">\n  <head>\n	<link rel='https://api.w.org/' href='http://www.tenfield.com.uy/wp-json/' />\n	<link rel=\"EditURI\" type=\"application/rsd+xml\" title=\"RSD\" href=\"http://www.tenfield.com.uy/xmlrpc.php?rsd\" />\n	<link rel=\"wlwmanifest\" type=\"application/wlwmanifest+xml\" href=\"http://www.tenfield.com.uy/wp-includes/wlwmanifest.xml\" /> \n	<meta name=\"generator\" content=\"WordPress 4.7.5\" />\n	<link rel='stylesheet' id='ngg_trigger_buttons-css'  href='http://www.tenfield.com.uy/wp-content/plugins/nextgen-gallery/products/photocrati_nextgen/modules/nextgen_gallery_display/static/trigger_buttons.min.css?ver=4.7.5' type='text/css' media='all' />\n	<link rel='stylesheet' id='fontawesome-css'  href='http://www.tenfield.com.uy/wp-content/plugins/nextgen-gallery/products/photocrati_nextgen/modules/nextgen_gallery_display/static/fontawesome/font-awesome.min.css?ver=4.7.5' type='text/css' media='all' />\n	<link rel='stylesheet' id='nextgen_basic_thumbnails_style-css'  href='http://www.tenfield.com.uy/wp-content/plugins/nextgen-gallery/products/photocrati_nextgen/modules/nextgen_basic_gallery/static/thumbnails/nextgen_basic_thumbnails.min.css?ver=4.7.5' type='text/css' media='all' />\n	<link rel='stylesheet' id='nextgen_pagination_style-css'  href='http://www.tenfield.com.uy/wp-content/plugins/nextgen-gallery/products/photocrati_nextgen/modules/nextgen_pagination/static/style.min.css?ver=4.7.5' type='text/css' media='all' />\n</head>\n<body id=\"index\">  \n	<div class=\"equiposBar hidden-xs visible-md visible-lg\">\n	<div class=\"container\">\n	<div class=\"row\">\n	<div class=\"escuditos\">\n	<a href=\"http://www.tenfield.com.uy/tag/boston-river/\"><span class=\"club boston\" style=\"margin-left:-15px;\"></span></a>\n	<a href=\"http://www.tenfield.com.uy/tag/cerro/\"><span class=\"club cerro\"></span></a>\n	<a href=\"http://www.tenfield.com.uy/tag/danubio/\"><span class=\"club danubio\"></span></a>\n	<a href=\"http://www.tenfield.com.uy/tag/defensor/\"><span class=\"club defensor\"></span></a>\n	<a href=\"http://www.tenfield.com.uy/tag/el-tanque-sisley/\"><span class=\"club tanque\"></span></a>\n	<a href=\"http://www.tenfield.com.uy/tag/juventud/\"><span class=\"club juventud\"></span></a>\n	<a href=\"http://www.tenfield.com.uy/tag/liverpool/\"><span class=\"club liverpool\"></span></a>\n	<a href=\"http://www.tenfield.com.uy/tag/nacional/\"><span class=\"club nacional\"></span></a>\n	<a href=\"http://www.tenfield.com.uy/tag/penarol/\"><span class=\"club penarol\"></span></a>\n	<a href=\"http://www.tenfield.com.uy/tag/plaza/\"><span class=\"club plaza\"></span></a>\n	<a href=\"http://www.tenfield.com.uy/tag/racing/\"><span class=\"club racing\"></span></a>\n	<a href=\"http://www.tenfield.com.uy/tag/rampla-juniors/\"><span class=\"club rampla\"></span></a>\n	<a href=\"http://www.tenfield.com.uy/tag/wanderers/\"><span class=\"club wanderers\"></span></a>\n	</div>\n	<div class=\"search hidden-xs hidden-md visible-lg\">\n</body>\n\n\n\n	\n";

    ByteBuffer b = ByteBuffer.wrap(data.getBytes());

    Mockito.when(dataFrame.getData())
        .thenReturn(b);

    Mockito.when(dataFrame.isEndStream())
        .thenReturn(true);

    http2SampleResult = new HTTP2SampleResult();

    http2StreamHandler = new HTTP2StreamHandler(http2Connection, url, null, null,
        http2SampleResult);

    HTTP2SampleResult resSR = http2StreamHandler.getHTTP2SampleResult();
    resSR.sampleStart();
    resSR.setPendingResponse(true);
    resSR.setResponseCode("200");
    resSR.setEmbebedResults(true);
    resSR.setSecondaryRequest(false);
    resSR.setEmbebedResultsDepth(1);
    resSR.setDataType(SampleResult.TEXT);
    resSR.setContentType("text/html");
    resSR.setURL(url);
    resSR.setEncodingAndType("UTF-8");
    resSR.setRequestHeaders(
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36\n");
    resSR.setResponseData(data.getBytes());
    resSR.setEmbeddedUrlRE("");

    http2StreamHandler.onData(stream, dataFrame, callback);

    resSR = http2StreamHandler.getHTTP2SampleResult();

    assertTrue(resSR.isSuccessful());
  }

  @Test
  public void onResetTest() {

    ResetFrame resetFrame = new ResetFrame(0, ErrorCode.REFUSED_STREAM_ERROR.code);
    http2SampleResult = buildThreadVarsResult();
    http2StreamHandler = new HTTP2StreamHandler(http2Connection, url, null,
        null, http2SampleResult);
    http2SampleResult.sampleStart();
    http2StreamHandler.onReset(stream, resetFrame);
    http2SampleResult = http2StreamHandler.getHTTP2SampleResult();
    assertEquals(String.valueOf(ErrorCode.REFUSED_STREAM_ERROR.code), http2SampleResult.getResponseCode());
  }

  public HTTP2SampleResult buildThreadVarsResult(){
    Mockito.when(threadVars.getObject(Mockito.any(String.class))).thenReturn(pack);
    return new HTTP2SampleResult(url,
        "", threadVars, 1,
        1, "");
  }
}
