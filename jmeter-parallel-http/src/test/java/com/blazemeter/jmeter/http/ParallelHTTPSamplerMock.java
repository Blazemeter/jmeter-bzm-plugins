package com.blazemeter.jmeter.http;

import org.apache.jmeter.protocol.http.sampler.HTTPAbstractImpl;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;

import java.net.URL;

public class ParallelHTTPSamplerMock extends ParallelHTTPSampler {
    public ParallelHTTPSamplerMock() {
        super();
        impl = new HCMock(this);
    }

    public class HCMock extends HTTPAbstractImpl {
        public HCMock(HTTPSamplerBase testElement) {
            super(testElement);
        }

        @Override
        protected HTTPSampleResult sample(URL url, String s, boolean b, int i) {
            HTTPSampleResult res = new HTTPSampleResult();
            res.setSuccessful(true);
            return res;
        }

        @Override
        public boolean interrupt() {
            return false;
        }
    }
}

