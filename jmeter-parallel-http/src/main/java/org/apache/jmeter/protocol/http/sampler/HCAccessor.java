package org.apache.jmeter.protocol.http.sampler;

import java.net.URL;

// due to JMeter's closed namespaces we have to have this class to access it
public class HCAccessor {
    public static HTTPAbstractImpl getInstance(HTTPSamplerBase sampler) {
        return new HTTPHC4Impl(sampler);
    }

    public static HTTPSampleResult sample(HTTPAbstractImpl impl, URL u, String method, boolean areFollowingRedirect, int depth) {
        return impl.sample(u, method, areFollowingRedirect, depth);
    }
}
