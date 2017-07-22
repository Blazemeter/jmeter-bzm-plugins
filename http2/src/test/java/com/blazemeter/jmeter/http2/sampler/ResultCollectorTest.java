package com.blazemeter.jmeter.http2.sampler;


import static org.junit.Assert.*;
import org.eclipse.jetty.http.HttpField;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieHandler;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.protocol.http.util.HTTPFileArgs;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.SearchableTreeNode;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.blazemeter.jmeter.http2.visualizers.ResultCollector;
import com.blazemeter.jmeter.http2.visualizers.ViewResultsFullVisualizer;

public class ResultCollectorTest {
	
	private ResultCollector http2ResultCollector; 
	
	
	@Before
    public void setup() {
		http2ResultCollector = new ResultCollector();
    }
	

	 @Test
	 public void sampleOccurredTest() throws Exception {
		 HTTP2SampleResult result = new HTTP2SampleResult();
		 HTTP2SampleResult child1 = new HTTP2SampleResult();
		 HTTP2SampleResult child2 = new HTTP2SampleResult();
		 child1.sampleStart();
		 child2.sampleStart();
		 result.sampleStart();
		 result.addSubResult(child1);
		 result.addSubResult(child2);
		 SampleEvent event = new SampleEvent((SampleResult) result, "threadGroup", "");
		 http2ResultCollector.setFilename("datawriter");
		 http2ResultCollector.testStarted();
		 http2ResultCollector.sampleOccurred(event);
		 http2ResultCollector.testEnded();
	 }
	 
}
