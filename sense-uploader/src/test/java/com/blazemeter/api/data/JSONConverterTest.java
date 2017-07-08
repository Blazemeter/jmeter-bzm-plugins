package com.blazemeter.api.data;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JSONConverterTest {


    @Test
    public void testConvert() throws Exception {
        List<SampleResult> list = new LinkedList<>();
        list.add(new SampleResult(System.currentTimeMillis(), 1));
        list.add(new SampleResult(System.currentTimeMillis() + 1000, 1));
        list.add(new SampleResult(System.currentTimeMillis() + 2000, 1));
        list.add(new SampleResult(System.currentTimeMillis() + 3000, 1));

        SampleResult res = new SampleResult(System.currentTimeMillis() + 3000, 3);
        res.setSampleLabel("L2");
        list.add(res);

        res = new SampleResult(System.currentTimeMillis() + 3000, 3);
        res.setSampleLabel("L2");
        list.add(res);

        res = new SampleResult(System.currentTimeMillis() + 5000, 2);
        res.setSampleLabel("L2");
        list.add(res);

        res = new SampleResult(System.currentTimeMillis() + 4000, 3);
        res.setSampleLabel("L2");
        list.add(res);


        JSONObject result = JSONConverter.convertToJSON(list, list);
        JSONArray labels = result.getJSONArray("labels");
        assertEquals(3, labels.size());
        
        for (Object obj : labels) {
            JSONObject label = (JSONObject) obj;
            String name = label.getString("name");
            if ("ALL".equals(name)) {
                assertEquals("8", label.getString("n"));
            } else if ("".equals(name)) {
                assertEquals("4", label.getString("n"));
            } else if ("L2".equals(name)) {
                assertEquals("4", label.getString("n"));
            } else {
                fail("Unexpected label name: " + name);
            }
        }
    }
}