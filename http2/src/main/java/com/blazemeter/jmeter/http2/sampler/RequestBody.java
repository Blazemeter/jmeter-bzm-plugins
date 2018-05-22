package com.blazemeter.jmeter.http2.sampler;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.util.EncoderCache;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

public class RequestBody {

    private static final String ARG_VAL_SEP = "=";
    private static final String QRY_SEP = "&";

    private static final Logger LOG = LoggerFactory.getLogger(RequestBody.class);

    private final String payload;
    private final String encoding;

    private RequestBody(String payload, String encoding) {
        this.payload = payload;
        this.encoding = encoding;
    }

    public static RequestBody from(String method, String contentEncoding, Arguments args, boolean sendParamsAsBody)
            throws UnsupportedEncodingException {
        return new RequestBody(buildPostBody(method, contentEncoding, args, sendParamsAsBody), contentEncoding);
    }

    private static String buildPostBody(String method, String contentEncoding, Arguments args, boolean sendParamsAsBody)
            throws UnsupportedEncodingException {
        if (HTTPConstants.POST.equals(method) && !sendParamsAsBody) {
            CollectionProperty arguments = args.getArguments();
            if (arguments.size() == 0) {
                return "";
            }

            if (JOrphanUtils.isBlank(contentEncoding)) {
                contentEncoding = EncoderCache.URL_ARGUMENT_ENCODING;
            }

            StringBuilder buf = new StringBuilder(arguments.size() * 15);
            PropertyIterator iter = arguments.iterator();
            boolean first = true;
            while (iter.hasNext()) {
                HTTPArgument item;
                /* Copied from jmeter http sampler code:
                 * N.B. Revision 323346 introduced the ClassCast check, but then used iter.next()
                 * to fetch the item to be cast, thus skipping the element that did not cast.
                 * Reverted to work more like the original code, but with the check in place.
                 * Added a warning message so can track whether it is necessary
                 */
                Object objectValue = iter.next().getObjectValue();
                if (objectValue instanceof HTTPArgument) {
                    item = (HTTPArgument) objectValue;
                } else {
                    LOG.warn("Unexpected argument type: " + objectValue.getClass().getName());
                    item = new HTTPArgument((Argument) objectValue);
                }
                final String encodedName = item.getEncodedName();
                if (encodedName.isEmpty()) {
                    continue; // Skip parameters with a blank name (allows use of optional variables in parameter lists)
                }
                if (!first) {
                    buf.append(QRY_SEP);
                } else {
                    first = false;
                }
                buf.append(encodedName);
                if (item.getMetaData() == null) {
                    buf.append(ARG_VAL_SEP);
                } else {
                    buf.append(item.getMetaData());
                }

                try {
                    buf.append(item.getEncodedValue(contentEncoding));
                } catch (UnsupportedEncodingException e) {
                    LOG.warn("Unable to encode parameter in encoding " + contentEncoding + ", parameter value not included in query string");
                }
            }
            return buf.toString();
        } else {
            StringBuilder postBodyBuffer = new StringBuilder();
            for (JMeterProperty jMeterProperty : args) {
                HTTPArgument arg = (HTTPArgument) jMeterProperty.getObjectValue();
                postBodyBuffer.append(arg.getEncodedValue(contentEncoding));
            }
            return postBodyBuffer.toString();
        }
    }

    public String getPayload() {
        return payload;
    }

    public byte[] getPayloadBytes() throws UnsupportedEncodingException {
        return payload.getBytes(encoding);
    }

}