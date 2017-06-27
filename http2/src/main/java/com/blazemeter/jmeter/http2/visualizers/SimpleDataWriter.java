package com.blazemeter.jmeter.http2.visualizers;

import java.awt.BorderLayout;

import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;



/**
 * This listener can record results to a file but not to the UI. It is meant to
 * provide an efficient means of recording data by eliminating GUI overhead.	
 */
public class SimpleDataWriter extends AbstractVisualizer{
    /* (non-Javadoc)
	 * @see org.apache.jmeter.visualizers.gui.AbstractVisualizer#setModel(org.apache.jmeter.reporters.ResultCollector)
	 */
	
	/* (non-Javadoc)
	 * @see org.apache.jmeter.visualizers.gui.AbstractVisualizer#setModel(org.apache.jmeter.reporters.ResultCollector)
	 */
	
	com.blazemeter.jmeter.http2.visualizers.ResultCollector resCollector = new com.blazemeter.jmeter.http2.visualizers.ResultCollector();
	
	@Override
	protected void setModel(ResultCollector collector) {
		// TODO Auto-generated method stub
		super.setModel(collector);
	}


	private static final long serialVersionUID = 240L;
	private int contador = 0;
    public static final String SIMPLE_DATA_WRITER_HTTP2_TITLE="Simple Data Writer Http2";
    private ListenerNotifier notifier;

    /**
     * Create the SimpleDataWriter.
     */
    public SimpleDataWriter() {
        init();
        setName(SIMPLE_DATA_WRITER_HTTP2_TITLE);
        setModel(resCollector);
        
    }

  
	
    @Override
    public String getLabelResource() {
        return SIMPLE_DATA_WRITER_HTTP2_TITLE; // $NON-NLS-1$
    }
    
    
    @Override
    public String getStaticLabel() {
        return SIMPLE_DATA_WRITER_HTTP2_TITLE;
    }


    /**
     * Initialize the component in the UI
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);
    }



    /**    
     * @param 
     */
    public void add(final SampleResult sample) {      	    	    
    	
  
    }
   
	@Override
	public void clearData() {
		// TODO Auto-generated method stub
		
	}
	

    
}
    