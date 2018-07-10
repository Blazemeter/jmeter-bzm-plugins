package com.blazemeter.jmeter.http2.visualizers;

import java.awt.BorderLayout;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;

public class SimpleDataWriter extends AbstractVisualizer {

  private static final String SIMPLE_DATA_WRITER_HTTP2_TITLE = "DEPRECATED Simple Data Writer Http2";

  public SimpleDataWriter() {
    init();
    setName(SIMPLE_DATA_WRITER_HTTP2_TITLE);
  }

  @Override
  public String getLabelResource() {
    return SIMPLE_DATA_WRITER_HTTP2_TITLE; // $NON-NLS-1$
  }


  @Override
  public String getStaticLabel() {
    return SIMPLE_DATA_WRITER_HTTP2_TITLE;
  }

  private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
    setLayout(new BorderLayout());
    setBorder(makeBorder());

    add(makeTitlePanel(), BorderLayout.NORTH);
  }

  @Override
  public void clearData() {

  }

  @Override
  public void add(SampleResult sampleResult) {

  }
}
