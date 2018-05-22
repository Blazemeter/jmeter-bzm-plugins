package com.blazemeter.jmeter.http2.sampler;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;

public class RequestBodyTest {

    @Test
    public void createPostContentTest() throws UnsupportedEncodingException {
        String text = "{\"header\":{\"applicationId\":\"HJS\"},\"initSession\":{}}";

        Arguments args = new Arguments();
        HTTPArgument arg = new HTTPArgument("", text.replaceAll("\n", "\r\n"), false);
        arg.setAlwaysEncoded(false);
        args.addArgument(arg);
        RequestBody requestBody = RequestBody.from(HTTPConstants.POST, HTTP2Request.ENCODING, args, true);

        assertEquals(text, requestBody.getPayload());
    }

}
