package com.blazemeter.jmeter.hls.logic;

import org.apache.jmeter.samplers.SampleResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import static org.apache.http.protocol.HTTP.USER_AGENT;

public class Parser implements Serializable {
    private static final Logger log = LoggingManager.getLoggerForClass();

    public Parser() {
    }

    // HTTP GET request
    public DataRequest getBaseUrl(URL url, SampleResult sampleResult, boolean setRequest) throws IOException {

	HttpURLConnection con = null;
	DataRequest result = new DataRequest();
	boolean first = true;
	long sentBytes = 0;

	con = (HttpURLConnection) url.openConnection();

	sampleResult.connectEnd();

	// By default it is GET request
	con.setRequestMethod("GET");

	// add request header
	con.setRequestProperty("User-Agent", USER_AGENT);

	// Set request header
	result.setRequestHeaders(con.getRequestMethod() + "  " + url.toString() + "\n");

	int responseCode = con.getResponseCode();

	// Reading response from input Stream
	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {

	    if (setRequest)
		response.append(inputLine + "\n");

	    sentBytes += inputLine.getBytes().length + 1;

	    if (first) {
		sampleResult.latencyEnd();
		first = false;
	    }
	}

	in.close();

	// Set response parameters
	result.setHeaders(con.getHeaderFields());
	result.setResponse(response.toString());
	result.setResponseCode(String.valueOf(responseCode));
	result.setResponseMessage(con.getResponseMessage());
	result.setContentType(con.getContentType());
	result.setSuccess(isSuccessCode(responseCode));
	result.setSentBytes(sentBytes);
	result.setContentEncoding(getEncoding(con));

	return result;

    }

    public String getEncoding(HttpURLConnection connection) {
	String contentType = connection.getContentType();
	String[] values = contentType.split(";"); // values.length should be 2
	String charset = "";

	for (String value : values) {
	    value = value.trim();

	    if (value.toLowerCase().startsWith("charset=")) {
		charset = value.substring("charset=".length());
	    }
	}

	return charset;
    }


    public List<DataFragment> extractVideoUrl(String playlistUrl) {
	String pattern = "EXTINF:(\\d+\\.?\\d*).*\\n(#.*:.*\\n)*(.*\\.ts(\\?.*\\n*)?)";
	final List<DataFragment> mediaList = new ArrayList<>();
	Pattern r = Pattern.compile(pattern);
	Matcher m = r.matcher(playlistUrl);
	while (m.find()) {
	    DataFragment data = new DataFragment(m.group(1), m.group(3));
	    log.info("index: " + m.group(1) + " fragment: " + m.group(3));
	    mediaList.add(data);
	}
	return mediaList;
    }

    private int resolutionCompare(String r1, String r2) {
	String[] r1Dimensions = r1.split("x");
	String[] r2Dimensions = r2.split("x");
	int a1 = Integer.parseInt(r1Dimensions[0]) * Integer.parseInt(r1Dimensions[1]);
	int a2 = Integer.parseInt(r2Dimensions[0]) * Integer.parseInt(r2Dimensions[1]);
	return Integer.compare(a1,a2);
    }

    private boolean resolutionOK(String streamResolution, String currentResolution, String matchMode, String customResolution){
	log.info("resolutionOK: " + streamResolution + ", " + currentResolution + ", " + matchMode + ", " + currentResolution);

	if(matchMode.equalsIgnoreCase("customResolution")){
	    if (customResolution != null) {
		return customResolution.equals(streamResolution);
	    }
	    log.error("selection mode is customResolution, but no custom resolution set");
	    return false;
	} else if(matchMode.equalsIgnoreCase("minResolution")){
	    if (currentResolution == null) {
		return true;
	    } else {
		if (streamResolution == null) return false;
		return (resolutionCompare(streamResolution,currentResolution) <= 0);
	    }
	} else if(matchMode.equalsIgnoreCase("maxResolution")){
	    if (currentResolution == null) {
		return true;
	    } else {
		if (streamResolution == null) return false;
		return (resolutionCompare(streamResolution,currentResolution) >= 0);
	    }

	}
	log.error("unknown resolution selection mode");
	return false;
    }
    
    public String extractMediaUrl(String playlistData, String customResolution, String customBandwidth, String bwSelected, String resSelected) {
	String streamPattern = "(EXT-X-STREAM-INF.*)\\n(.*\\.m3u8.*)";
	String bandwidthPattern = "[:|,]BANDWIDTH=(\\d+)";
	String resolutionPattern = "[:|,]RESOLUTION=(\\d+x\\d+)";

	return getMediaUrl(streamPattern, bandwidthPattern, resolutionPattern, playlistData, customResolution, customBandwidth, bwSelected, resSelected);
    }


    public String getMediaUrl(String streamPattern, String bandwidthPattern, String resolutionPattern, String playlistData,
			      String customResolution, String customBandwidth, String bwSelected, String resSelected) {
	String bandwidthMax ="100000000";
	String resolutionMin = "100x100";
	String resolutionMax = "5000x5000";
	String curBandwidth = null;
	String curResolution = null;
	String uri = null;
		
	Pattern s = Pattern.compile(streamPattern);
	Matcher m = s.matcher(playlistData);

	Pattern b = Pattern.compile(bandwidthPattern);
	Pattern r = Pattern.compile(resolutionPattern);
		
	while (m.find()) {
	    Matcher mr = r.matcher(m.group(1));
	    boolean rfound = mr.find();
	    Matcher mb = b.matcher(m.group(1));
	    boolean bfound = mb.find();

	    if (! bfound) {
		continue;
	    }

	    if (bwSelected.equalsIgnoreCase("customBandwidth")) {
		if (Integer.parseInt(mb.group(1)) == Integer.parseInt(customBandwidth)) {
		    if (resolutionOK((rfound?mr.group(1):null), curResolution, resSelected, customResolution)) {
			curResolution = (rfound?mr.group(1):null);
			uri = m.group(2);
		    }
		}
	    } else if (bwSelected.equalsIgnoreCase("minBandwidth")) {
		if (curBandwidth == null || (Integer.parseInt(mb.group(1)) <= Integer.parseInt(curBandwidth))) {
		    curBandwidth = mb.group(1);
		    if (resolutionOK((rfound?mr.group(1):null), curResolution, resSelected, customResolution)) {
			curResolution =(rfound?mr.group(1):null);
			uri = m.group(2);
		    }
		}

	    } else if (bwSelected.equalsIgnoreCase("maxBandwidth")) {
		if (curBandwidth == null || (Integer.parseInt(mb.group(1)) >= Integer.parseInt(curBandwidth))) {
		    curBandwidth = mb.group(1);
		    if (resolutionOK((rfound?mr.group(1):null), curResolution, resSelected, customResolution)) {
			curResolution = (rfound?mr.group(1):null);
			uri = m.group(2);
		    }
		}

	    } else {
		log.error("unknown bandwidth selection mode");
	    }

	}
	return uri;

    }

    public boolean isLive(String playlistUrl) {
	String pattern1 = "EXT-X-ENDLIST";
	Pattern r1 = Pattern.compile(pattern1);
	Matcher m1 = r1.matcher(playlistUrl);

	return !m1.find();
    }

    protected boolean isSuccessCode(int code) {
	return code >= 200 && code <= 399;
    }

}
