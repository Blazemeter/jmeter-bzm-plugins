package com.blazemeter.jmeter.hls.logic;

import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class HlsSampler extends AbstractSampler {
	private static final Logger log = LoggingManager.getLoggerForClass();
	private String playlistUri;
	private ArrayList<String> fragmentsDownloaded = new ArrayList<>();
	public static final String HEADER_MANAGER = "HLSRequest.header_manager"; // $NON-NLS-1$
	public static final String COOKIE_MANAGER = "HLSRequest.cookie_manager"; // $NON-NLS-1$
	public static final String CACHE_MANAGER = "HLSRequest.cache_manager"; // $NON-NLS-1$
	private Parser parser;

	private String playlist;

	public HlsSampler() {
		super();
		setName("HLS Sampler");
		parser = new Parser();
	}

	public HeaderManager getHeaderManager() {
		return (HeaderManager) getProperty(HlsSampler.HEADER_MANAGER).getObjectValue();
	}

	private DataRequest getMasterList(SampleResult masterResult, Parser parser) throws IOException {

		masterResult.sampleStart();
		DataRequest respond = parser.getBaseUrl(new URL(getURLData()), masterResult, true);
		masterResult.sampleEnd();

		masterResult.setRequestHeaders(respond.getRequestHeaders() + "\n\n" + getCookieHeader(getURLData()) + "\n\n"
				+ getRequestHeader(this.getHeaderManager()));
		masterResult.setSuccessful(respond.isSuccess());
		masterResult.setResponseMessage(respond.getResponseMessage());
		masterResult.setSampleLabel(this.getName());
		masterResult.setResponseHeaders(respond.getHeadersAsString());
		masterResult.setResponseData(respond.getResponse().getBytes());
		masterResult.setResponseCode(respond.getResponseCode());
		masterResult.setContentType(respond.getContentType());
		masterResult.setBytes(masterResult.getBytesAsLong() + (long) masterResult.getRequestHeaders().length());

		int headerBytes = masterResult.getResponseHeaders().length() // condensed
																		// length
																		// (without
																		// \r)
				+ respond.getHeaders().size() // Add \r for each header
				+ 1 // Add \r for initial header
				+ 2; // final \r\n before data

		masterResult.setHeadersSize((int) headerBytes);
		masterResult.setSentBytes(respond.getSentBytes());
		masterResult.setDataEncoding(respond.getContentEncoding());

		return respond;

	}

	private String getPlaylistPath(DataRequest respond, Parser parser) throws MalformedURLException {
		URL masterURL = new URL(getURLData());
		playlistUri = parser.extractMediaUrl(respond.getResponse(), this.getRESDATA(), this.getNetwordData(),
				this.getBandwidthType(), this.getResolutionType());
		String auxPath = masterURL.getPath().substring(0, masterURL.getPath().lastIndexOf('/') + 1);
		
		if(playlistUri == null)
			playlistUri = getURLData();

		if (playlistUri.startsWith("http")) {
			playlist = playlistUri;
		} else if (playlistUri.indexOf('/') == 0) {
			playlist = getPRotocol() + "://" + masterURL.getHost() + (masterURL.getPort() > 0 ? ":" + masterURL.getPort() : "") + playlistUri;// "https://"
		} else {
			playlist = getPRotocol() + "://" + masterURL.getHost() + (masterURL.getPort() > 0 ? ":" + masterURL.getPort() : "") + auxPath + playlistUri;
		}

		auxPath = getPRotocol() + "://" + masterURL.getHost() + (masterURL.getPort() > 0 ? ":" + masterURL.getPort() : "") + auxPath;

		return auxPath;

	}

	private DataRequest getPlayList(SampleResult playListResult, Parser parser) throws IOException {

		String lastPath = "";
		playListResult.sampleStart();
		DataRequest subRespond = parser.getBaseUrl(new URL(playlist), playListResult, true);
		playListResult.sampleEnd();

		String[] urlArray = playlist.split("/");
		lastPath = urlArray[urlArray.length - 1];

		playListResult.setRequestHeaders(subRespond.getRequestHeaders() + "\n\n" + getCookieHeader(playlist) + "\n\n"
				+ getRequestHeader(this.getHeaderManager()));
		playListResult.setSuccessful(subRespond.isSuccess());
		playListResult.setResponseMessage(subRespond.getResponseMessage());
		playListResult.setSampleLabel(lastPath);
		playListResult.setResponseHeaders(subRespond.getHeadersAsString());
		playListResult.setResponseData(subRespond.getResponse().getBytes());
		playListResult.setResponseCode(subRespond.getResponseCode());
		playListResult.setContentType(subRespond.getContentType());
		playListResult.setBytes(playListResult.getBytesAsLong() + (long) playListResult.getRequestHeaders().length());

		int headerBytes = playListResult.getResponseHeaders().length() // condensed
																		// length
																		// (without
																		// \r)
				+ subRespond.getHeaders().size() // Add \r for each header
				+ 1 // Add \r for initial header
				+ 2; // final \r\n before data

		playListResult.setHeadersSize((int) headerBytes);
		playListResult.setSentBytes(subRespond.getSentBytes());
		playListResult.setDataEncoding(subRespond.getContentEncoding());

		return subRespond;
	}

	@Override
	public SampleResult sample(Entry e) {
		SampleResult masterResult = new SampleResult();
		float currenTimeseconds = 0;
		boolean isVod = getHlsVideoType().equals("vod");
		boolean out = false;
		boolean firstTime = true;
		boolean containNewFragments = false;
		List<String> list = new ArrayList<>();

		try {

			DataRequest respond = getMasterList(masterResult, parser);
			String auxPath = getPlaylistPath(respond, parser);

			int playSeconds = 0;
			if (!getPlAYSecondsData().isEmpty())
				playSeconds = Integer.parseInt(getPlAYSecondsData());


			while ((playSeconds >= currenTimeseconds) && !out) {

				SampleResult playListResult = new SampleResult();
				DataRequest subRespond = getPlayList(playListResult, parser);

				List<DataFragment> videoUri = parser.extractVideoUrl(subRespond.getResponse());
				List<DataFragment> fragmentToDownload = new ArrayList<>();

				if (firstTime) {
					if (!(((getHlsVideoType().equals("live")) && (parser.isLive(subRespond.getResponse())))
					      || ((isVod) && (!parser.isLive(subRespond.getResponse())))
					      || ((getHlsVideoType().equals("event")) && (parser.isLive(subRespond.getResponse()))))) {
					    
					} else {
						firstTime = false;
						out = isVod;
					}

				}

				while ((!videoUri.isEmpty()) && (playSeconds >= currenTimeseconds)) {
					DataFragment frag = videoUri.remove(0);

					boolean isPresent = false;
					int length = fragmentsDownloaded.size();

					if (length != 0) {
						isPresent = fragmentsDownloaded.contains(frag.getTsUri().trim());
					}

					if (!isPresent) {
						fragmentToDownload.add(frag);
						fragmentsDownloaded.add(frag.getTsUri().trim());
						containNewFragments = true;
						if(getVideoDuration()) {
							currenTimeseconds += Float.parseFloat(frag.getDuration());
						}
					}
				}

				List<SampleResult> videoFragment = getFragments(parser, fragmentToDownload, auxPath);
				for (SampleResult sam : videoFragment) {
					playListResult.addSubResult(sam);
				}

				if((!list.contains(playListResult.getSampleLabel())) || (list.contains(playListResult.getSampleLabel()) && containNewFragments))
				{
					masterResult.addSubResult(playListResult);
					list.add(playListResult.getSampleLabel());
					containNewFragments = false;
				}

			}

		} catch (IOException e1) {
			e1.printStackTrace();
			masterResult.sampleEnd();
			masterResult.setSuccessful(false);
			masterResult.setResponseMessage("Exception: " + e1);
		}
		return masterResult;
	}


	public String getURLData() {
		return this.getPropertyAsString("HLS.URL_DATA");
	}

	public String getRESDATA() {
		return this.getPropertyAsString("HLS.RES_DATA");
	}

	public String getNetwordData() {
		return this.getPropertyAsString("HLS.NET_DATA");
	}

	public String getPlAYSecondsData() {
		return this.getPropertyAsString("HLS.SECONDS_DATA");
	}

	public boolean getVideoDuration() {
		return this.getPropertyAsBoolean("HLS.DURATION");
	}

	public String getVideoType() {
		return this.getPropertyAsString("HLS.VIDEOTYPE");
	}

	public String getResolutionType() {
		return this.getPropertyAsString("HLS.RESOLUTION_TYPE");
	}

	public String getBandwidthType() {
		return this.getPropertyAsString("HLS.BANDWIDTH_TYPE");
	}

	public String getHlsVideoType() {
		return this.getPropertyAsString("HLS.VIDEOTYPE");
	}

	public String getPRotocol() {
		return this.getPropertyAsString("HLS.PROTOCOL");
	}

	public void setURLData(String url) {

		this.setProperty("HLS.URL_DATA", url);
	}

	public void setResData(String res) {

		this.setProperty("HLS.RES_DATA", res);
	}

	public void setNetworkData(String net) {
		this.setProperty("HLS.NET_DATA", net);
	}

	public void setVideoDuration(boolean res) {
		this.setProperty("HLS.DURATION", res);
	}

	public void setPlaySecondsData(String seconds) {

		this.setProperty("HLS.SECONDS_DATA", seconds);
	}

	public void setPRotocol(String protocolValue) {
		this.setProperty("HLS.PROTOCOL", protocolValue);
	}

	public void setHlsDuration(String duration) {
		this.setProperty("HLS.DURATION", duration);
	}

	public void setResolutionType(String type) {
		this.setProperty("HLS.RESOLUTION_TYPE", type);
	}

	public void setBandwidthType(String type) {
		this.setProperty("HLS.BANDWIDTH_TYPE", type);
	}

	public void setHlsVideoType(String type) {
		this.setProperty("HLS.VIDEOTYPE", type);
	}

	public void setUrlVideoType(String type) {
		this.setProperty("HLS.URLVIDEOTYPE", type);
	}

	public List<SampleResult> getFragments(Parser parser, List<DataFragment> uris, String url) {
		List<SampleResult> res = new ArrayList<>();

		if (!uris.isEmpty()) {
			SampleResult result = new SampleResult();
			String uriString = uris.get(0).getTsUri();
			if ((url != null) && (!uriString.startsWith("http"))) {
				uriString = url + uriString;
			}
			//log.info("fragment URI: " + uriString);
			
			result.sampleStart();

			try {

				DataRequest respond = parser.getBaseUrl(new URL(uriString), result, false);

				result.sampleEnd();

				String[] urlArray = uriString.split("/");
				String lastPath = urlArray[urlArray.length - 1];

				result.setRequestHeaders(respond.getRequestHeaders() + "\n\n" + getCookieHeader(uriString) + "\n\n"
						+ getRequestHeader(this.getHeaderManager()));
				result.setSuccessful(respond.isSuccess());
				result.setResponseMessage(respond.getResponseMessage());
				result.setSampleLabel(lastPath);
				result.setResponseHeaders("URL: " + uriString + "\n" + respond.getHeadersAsString());
				result.setResponseCode(respond.getResponseCode());
				result.setContentType(respond.getContentType());
				result.setBytes(result.getBytesAsLong() + (long) result.getRequestHeaders().length());
				int headerBytes = result.getResponseHeaders().length() // condensed
																		// length
																		// (without
																		// \r)
						+ respond.getHeaders().size() // Add \r for each header
						+ 1 // Add \r for initial header
						+ 2; // final \r\n before data

				result.setHeadersSize((int) headerBytes);
				result.setSentBytes(respond.getSentBytes());
				result.setDataEncoding(respond.getContentEncoding());

				res.add(result);

			} catch (IOException e1) {
				e1.printStackTrace();
				result.sampleEnd();
				result.setSuccessful(false);
				result.setResponseMessage("Exception: " + e1);
				res.add(result);
			}

			uris.remove(0);
			List<SampleResult> aux = getFragments(parser, uris, url);
			for (SampleResult s : aux) {
				if(!res.contains(s))
					res.add(s);
			}
		}
		return res;
	}

	// private method to allow AsyncSample to reset the value without performing
	// checks
	private void setCookieManagerProperty(CookieManager value) {
		setProperty(new TestElementProperty(COOKIE_MANAGER, value));
	}

	public void setCookieManager(CookieManager value) {
		CookieManager mgr = getCookieManager();
		if (mgr != null) {
			log.warn("Existing CookieManager " + mgr.getName() + " superseded by " + value.getName());
		}
		setCookieManagerProperty(value);
	}

	public CookieManager getCookieManager() {
		return (CookieManager) getProperty(COOKIE_MANAGER).getObjectValue();
	}

	public void setCacheManager(CacheManager value) {
		CacheManager mgr = getCacheManager();
		if (mgr != null) {
			log.warn("Existing CacheManager " + mgr.getName() + " superseded by " + value.getName());
		}
		setCacheManagerProperty(value);
	}

	// private method to allow AsyncSample to reset the value without performing
	// checks
	private void setCacheManagerProperty(CacheManager value) {
		setProperty(new TestElementProperty(CACHE_MANAGER, value));
	}

	public CacheManager getCacheManager() {
		return (CacheManager) getProperty(CACHE_MANAGER).getObjectValue();
	}

	public String getRequestHeader(org.apache.jmeter.protocol.http.control.HeaderManager headerManager) {
		String headerString = "";

		if (headerManager != null) {
			CollectionProperty headers = headerManager.getHeaders();
			if (headers != null) {
				for (JMeterProperty jMeterProperty : headers) {
					org.apache.jmeter.protocol.http.control.Header header = (org.apache.jmeter.protocol.http.control.Header) jMeterProperty
							.getObjectValue();
					String n = header.getName();
					if (!HTTPConstants.HEADER_CONTENT_LENGTH.equalsIgnoreCase(n)) {
						String v = header.getValue();
						v = v.replaceFirst(":\\d+$", "");
						headerString = headerString + n + ": " + v + "\n";
					}
				}
			}
		}

		return headerString;
	}

	public void setHeaderManager(HeaderManager value) {
		HeaderManager mgr = getHeaderManager();
		if (mgr != null) {
			value = mgr.merge(value, true);
			if (log.isDebugEnabled()) {
				log.debug("Existing HeaderManager '" + mgr.getName() + "' merged with '" + value.getName() + "'");
				for (int i = 0; i < value.getHeaders().size(); i++) {
					log.debug("    " + value.getHeader(i).getName() + "=" + value.getHeader(i).getValue());
				}
			}
		}
		setProperty(new TestElementProperty(HEADER_MANAGER, (TestElement) value));
	}

	public String getCookieHeader(String urlData) throws MalformedURLException {
		String headerString = "";

		URL url = new URL(urlData);
		// Extracts all the required cookies for that particular URL request
		String cookieHeader = null;
		if (getCookieManager() != null) {
			cookieHeader = getCookieManager().getCookieHeaderForURL(url);
			if (cookieHeader != null) {
				headerString = headerString + HTTPConstants.HEADER_COOKIE + ": " + cookieHeader + "\n";
			}
		}

		return headerString;
	}

	@Override
	public void addTestElement(TestElement el) {
		if (el instanceof HeaderManager) {
			setHeaderManager((HeaderManager) el);
		} else if (el instanceof CookieManager) {
			setCookieManager((CookieManager) el);
		} else if (el instanceof CacheManager) {
			setCacheManager((CacheManager) el);
		} else {
			super.addTestElement(el);
		}
	}
	
	public void setParser (Parser p){
		parser = p;
	}

}
