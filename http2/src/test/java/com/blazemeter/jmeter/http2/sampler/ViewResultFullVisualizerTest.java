package com.blazemeter.jmeter.http2.sampler;


import static org.junit.Assert.*;


import java.awt.event.ActionEvent;


import javax.swing.tree.DefaultMutableTreeNode;


import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.samplers.SampleResult;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.SearchableTreeNode;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.blazemeter.jmeter.http2.visualizers.ViewResultsFullVisualizer;

import kg.apc.emulators.TestJMeterUtils;

public class ViewResultFullVisualizerTest {
	
	private ViewResultsFullVisualizer http2ViewResTree; 
	
	
	@Before
    public void setup() {
		TestJMeterUtils.createJmeterEnv();
		JMeterUtils.setProperty("view.results.tree.max_size", "19");
		JMeterUtils.setProperty("view.results.tree.renderers_order", ".RenderAsText,.RenderAsRegexp,.RenderAsCssJQuery,.RenderAsXPath,org.apache.jmeter.extractor.json.render.RenderAsJsonRenderer,.RenderAsHTML,.RenderAsHTMLFormatted,.RenderAsHTMLWithEmbedded,.RenderAsDocument,.RenderAsJSON,.RenderAsXML");
		http2ViewResTree = new ViewResultsFullVisualizer();
    }
	

	 @Test
	 public void findNodeTest() throws Exception {
		 int id= 2;
		 DefaultMutableTreeNode node = http2ViewResTree.findNode(http2ViewResTree.getRoot(), 2);
		 
		 assertEquals(null, node);
		 
		 // add an HTTP2 node
		 HTTP2SampleResult http2Child = new HTTP2SampleResult();
		 http2Child.setId(2);
		 DefaultMutableTreeNode leafNode = new SearchableTreeNode(http2Child, null);
		 http2ViewResTree.getTreeModel().insertNodeInto(leafNode, http2ViewResTree.getRoot(), http2ViewResTree.getRoot().getChildCount());
		 node = http2ViewResTree.findNode(http2ViewResTree.getRoot(), 2);
		 
		 assertEquals(leafNode, node);
		 
	 }
	 
	 @Test
	 public void actionPerformedTest() {
		ActionEvent event= new ActionEvent(http2ViewResTree.getSelectRenderPanel(),1001,"change_combo");
		http2ViewResTree.actionPerformed(event);
		
	 }
	 
	 @Test
	 public void getResponseAsStringTest() {
		 HTTP2SampleResult http2SampleRes= new HTTP2SampleResult();
		 http2SampleRes.setDataType(SampleResult.TEXT);
		 String textExp = "Convert Java String";
		 byte[] bytes = textExp.getBytes();
		 http2SampleRes.setResponseData(bytes);
		 String textRes = http2ViewResTree.getResponseAsString(http2SampleRes);
		 
		 assertEquals(textExp, textRes);
		 
		 
		 HTTP2SampleResult http2SampleRes2= new HTTP2SampleResult();
		 textExp = "Convert Java String and more text";
		 bytes = textExp.getBytes();
		 http2SampleRes2.setResponseData(bytes);
		 textRes = http2ViewResTree.getResponseAsString(http2SampleRes2);
		 
		 assertTrue(textRes.contains("Response too large to be displayed."));
		 
		 
	 }
	 
	 @Test
	 public void addSubResultsTest() {
		 HTTP2SampleResult result = new HTTP2SampleResult();
		 result.sampleStart();
		 
		 //HTTP1 child
		 HTTPSampleResult child1 = new HTTPSampleResult();
		 child1.sampleStart();
		 AssertionResult assertionResult1 = new AssertionResult("assertion1");
		 assertionResult1.setFailure(true);
		 child1.addAssertionResult(assertionResult1);
		 
		 
		 // HTTP2 child Secondary Request
		 HTTP2SampleResult child2 = new HTTP2SampleResult();
		 child2.sampleStart();
		 child2.setSecondaryRequest(true);
		 AssertionResult assertionResult2 = new AssertionResult("assertion2");
		 assertionResult2.setFailure(true);
		 child2.addAssertionResult(assertionResult2);
		 AssertionResult assertionResult3 = new AssertionResult("assertion3");
		 assertionResult3.setError(true);
		 child2.addAssertionResult(assertionResult3);
		 
		// HTTP2 child Secondary Request
		 HTTP2SampleResult child3 = new HTTP2SampleResult();
		 child3.sampleStart();
		 child3.setSecondaryRequest(false);
		 AssertionResult assertionResult4 = new AssertionResult("assertion4");
		 assertionResult4.setFailure(true);
		 child3.addAssertionResult(assertionResult4);
		 
		 		 
		 
		 result.addSubResult(child1);
		 result.addSubResult(child2);
		 result.addSubResult(child3);
		 
		 
		 DefaultMutableTreeNode currNode = new SearchableTreeNode(result, http2ViewResTree.getTreeModel());
		 http2ViewResTree.getTreeModel().insertNodeInto(currNode, http2ViewResTree.getRoot(), http2ViewResTree.getRoot().getChildCount());
		 http2ViewResTree.addSubResults(currNode, result);
	 }
	 
 
}
