package com.blazemeter.jmeter.http2.sampler;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import org.apache.jmeter.protocol.http.util.HTTPResultConverter;
import org.apache.jmeter.save.converters.SampleResultConverter;

public class HTTP2ResultConverter extends SampleResultConverter {

  private HTTPResultConverter httpResultConverter;

  public HTTP2ResultConverter(Mapper arg0) {
    super(arg0);
    httpResultConverter = new HTTPResultConverter(arg0);
  }

  @Override
  public boolean canConvert(
      @SuppressWarnings("rawtypes") Class arg0) { // superclass does not support types
    return HTTP2SampleResult.class.equals(arg0);
  }

  @Override
  public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
    httpResultConverter.marshal(obj, writer, context);
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    return httpResultConverter.unmarshal(reader, context);
  }
}
