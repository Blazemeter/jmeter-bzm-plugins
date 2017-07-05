package com.blazemeter.api.data;

import net.sf.json.JSONObject;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

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
        // TODO: assertEquals
        System.out.println(result);
    }
}