package com.blazemeter.jmeter.http2.visualizers;

import java.awt.BorderLayout;
import java.util.Collection;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;

public class SimpleDataWriter extends org.apache.jmeter.visualizers.SimpleDataWriter {

  private static final String SIMPLE_DATA_WRITER_HTTP2_TITLE = "DEPRECATED Simple Data Writer Http2";

  public SimpleDataWriter() {
    super();
    setName(SIMPLE_DATA_WRITER_HTTP2_TITLE);
  }

  public Collection<String> getMenuCategories() {
    Collection<String> categories = super.getMenuCategories();
    categories.remove(SIMPLE_DATA_WRITER_HTTP2_TITLE);
    return categories;
  }

  @Override
  public String getLabelResource() {
    return SIMPLE_DATA_WRITER_HTTP2_TITLE; // $NON-NLS-1$
  }


  @Override
  public String getStaticLabel() {
    return SIMPLE_DATA_WRITER_HTTP2_TITLE;
  }
}
