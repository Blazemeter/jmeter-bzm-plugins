package com.blazemeter.jmeter.http;

import kg.apc.emulators.TestJMeterUtils;
import kg.apc.jmeter.JMeterPluginsUtils;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.protocol.http.sampler.ParallelHTTPSampler;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParallelHTTPSamplerTest {
    @BeforeClass
    public static void setUp() throws Exception {
        TestJMeterUtils.createJmeterEnv();
        JMeterUtils.setProperty("HTTPResponse.parsers", "htmlParser");
        JMeterUtils.setProperty("htmlParser.types", "text/html");
        JMeterUtils.setProperty("htmlParser.className", "org.apache.jmeter.protocol.http.parser.LagartoBasedHtmlParser");
    }

    @Test
    public void sample() throws Exception {
        ParallelHTTPSampler obj = new ParallelHTTPSampler();
        obj.setConcurrentDwn(false); //FIXME: remove/comment this
        PowerTableModel dataModel = new PowerTableModel(ParallelHTTPSampler.columnIdentifiers, ParallelHTTPSampler.columnClasses);
        dataModel.addRow(new String[]{"http://localhost:8000/rtimes/const?delay=1"});
        dataModel.addRow(new String[]{"http://localhost:8000/rtimes/const?delay=2"});
        CollectionProperty prop = JMeterPluginsUtils.tableModelRowsToCollectionProperty(dataModel, ParallelHTTPSampler.DATA_PROPERTY);
        obj.setData(prop);
        SampleResult res = obj.sample();
        assertTrue(res.isSuccessful());
        assertEquals(2, res.getSubResults().length);
    }
}