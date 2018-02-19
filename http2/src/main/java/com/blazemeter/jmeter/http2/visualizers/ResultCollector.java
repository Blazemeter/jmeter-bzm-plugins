package com.blazemeter.jmeter.http2.visualizers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import com.blazemeter.jmeter.http2.sampler.HTTP2SampleResult;

public class ResultCollector extends org.apache.jmeter.reporters.ResultCollector {

	private static final long serialVersionUID = 234L;

	public ResultCollector() {
		this(null);
	}

	public ResultCollector(Summariser summer) {
		super(summer);
	}

	@Override
	public void sampleOccurred(SampleEvent event) {

		SampleResult result = event.getResult();

		// Verify if it's an HTTPSampleResult
		if (result instanceof HTTP2SampleResult) {
			HTTP2SampleResult http2Result = (HTTP2SampleResult) result;

			if (!http2Result.isPendingResponse()) {

				List<SampleEvent> events = new ArrayList<>();
				events.add(event);

				// Look into childs to get the response of the message and the
				// response of the secondary requests
				for (HTTP2SampleResult child : http2Result.getPendingResults()) {
					if (!http2Result.isSecondaryRequest()) {
						SampleEvent evtChild = new SampleEvent(child, event.getThreadGroup());
						events.add(evtChild);
					}
				}

				Collections.sort(events,
						(e1, e2) -> Long.compare(e1.getResult().getEndTime(), e1.getResult().getEndTime()));

				for (SampleEvent e : events) {
					super.sampleOccurred(e);
				}
			}
		} else {
			super.sampleOccurred(event);
		}
	}
}