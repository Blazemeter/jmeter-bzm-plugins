package com.blazemeter.jmeter.http;

import org.apache.jmeter.protocol.http.sampler.HCAccessor;
import org.apache.jmeter.protocol.http.sampler.HTTPAbstractImpl;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.util.ArrayList;

public class ParallelHTTPSampler extends HTTPSamplerBase implements Interruptible {
    private static final Logger log = LoggingManager.getLoggerForClass();
    public static final String DATA_PROPERTY = "urls";
    public static final String[] columnIdentifiers = new String[]{
            "URL",
    };
    public static final Class[] columnClasses = new Class[]{
            String.class,
    };
    protected transient HTTPAbstractImpl impl;

    public ParallelHTTPSampler() {
        super();
        setImageParser(true); // force download embedded resources
        setConcurrentDwn(true); // force concurrent download
    }

    public ParallelHTTPSampler(String impl) {
        super();
        setImplementation(impl);
    }

    @Override
    protected HTTPSampleResult sample(java.net.URL u, String method, boolean areFollowingRedirect, int depth) {
        if (depth < 1) {
            JMeterProperty data = getData();
            StringBuilder body = new StringBuilder();
            StringBuilder req = new StringBuilder();
            if (!(data instanceof NullProperty)) {
                CollectionProperty rows = (CollectionProperty) data;

                for (JMeterProperty row : rows) {
                    ArrayList<Object> curProp = (ArrayList<Object>) row.getObjectValue();
                    req.append(curProp.get(0)).append("\n");
                    body.append("<iframe src='").append(curProp.get(0)).append("'></iframe>\n");
                }
            }

            log.info("Body: " + body.toString());
            HTTPSampleResult res = new HTTPSampleResult();
            res.setSamplerData(req.toString());
            res.setSuccessful(true);
            res.setResponseData(body.toString(), res.getDataEncodingWithDefault());
            res.setContentType("text/html");
            res.sampleStart();
            downloadPageResources(res, res, depth);
            if (res.getEndTime() == 0L) {
                res.sampleEnd();
            }
            return res;
        } else {
            if (impl == null) {
                impl = HCAccessor.getInstance(this);
            }

            return HCAccessor.sample(impl, u, method, areFollowingRedirect, depth);
        }
    }

    @Override
    public boolean interrupt() {
        return false;
    }

    public void setData(CollectionProperty rows) {
        setProperty(rows);
    }

    public JMeterProperty getData() {
        return getProperty(DATA_PROPERTY);
    }
}
