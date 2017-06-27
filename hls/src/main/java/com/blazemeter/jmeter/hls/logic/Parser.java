package com.blazemeter.jmeter.hls.logic;

import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.http.protocol.HTTP.USER_AGENT;


public class Parser {
    private float duration = -1;
    private float actualDuration = 0;
    private static final Pattern PORT_PATTERN = Pattern.compile("\\d+");
    private static final String USER_TOKEN = "__jmeter.USER_TOKEN__"; //$NON-NLS-1$
    private static final Logger log = LoggingManager.getLoggerForClass();
    private static final String PROXY_CONNECTION = "proxy-connection"; // $NON-NLS-1$
    private final Map<String, String> headers = new HashMap<>();


    HttpContext localContext = new BasicHttpContext();


    public Parser() {
    }


    // HTTP GET request
    public DataRequest getBaseUrl(String urlData, SampleResult sampleResult, boolean setRequest) throws IOException {

        HttpURLConnection con = null;
        DataRequest result = new DataRequest();
        boolean first = true;
        long sentBytes = 0;

        if (urlData == null) return null;
        URL url = new URL(urlData);

        con = (HttpURLConnection) url.openConnection();

        sampleResult.connectEnd();

        // By default it is GET request
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);


        //Set request header
        result.setRequestHeaders(con.getRequestMethod() + "  " + urlData + "\n");

        /*for (String header : con.getRequestProperties().keySet()) {
            if (header != null) {
                for (String value : con.getRequestProperties().get(header)) {
                    result.setRequestHeaders(result.getRequestHeaders() + header + ":" + value + "\n");
                }
            }
        }*/



        int responseCode = con.getResponseCode();


        // Reading response from input Stream
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String inputLine;
        StringBuffer response = new StringBuffer();
        
        while ((inputLine = in.readLine()) != null) {

            if(setRequest)
                response.append(inputLine + "\n");

            sentBytes += inputLine.getBytes().length + 2;

            if (first) {
                sampleResult.latencyEnd();
                first = false;
            }
        }

        in.close();

        
        //Set response parameters
        result.setHeaders(con.getHeaderFields());
        result.setResponse(response.toString());
        result.setResponseCode(String.valueOf(responseCode));
        result.setResponseMessage(con.getResponseMessage());
        result.setContentType(con.getContentType());
        result.setSuccess(isSuccessCode(responseCode));
        result.setSentBytes(sentBytes);//TODO consultar
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

    public String extractUriMaster(String res, String resolution, String bandwidth, String bandSelected, String resolSelected, String urlVideoType) {
//        String pattern = "(EXT-X-STREAM-INF.*)[:|,]BANDWIDTH=(\\d*).*RESOLUTION=(\\d*x\\d*).*\\n(.*)";//"(EXT-X-STREAM-INF.*)BANDWIDTH=(\\d*).*RESOLUTION=(\\d*x\\d*).*\\n.*?((url_\\d*){0,1}\\/.*\\.m3u8.*)";//(EXT-X-STREAM-INF.*)BANDWIDTH=(\\d*).*RESOLUTION=(\\d*x\\d*).*\\n.*?(\\/.*\\.m3u8.*)";
        String pattern = "(EXT-X-STREAM-INF.*)\\n(.*\\.m3u8.*)";
        String bandwidthPattern = "[:|,]BANDWIDTH=(\\d*)";
        String resolutionPattern = "[:|,]RESOLUTION=(\\d*x\\d*)";

        String urlCandidates = "";
        String secResolution = " ";
        String secBandwidth = " ";

        String bandwidthMax = "100000000";
        String bandwidthMin = "0";
        String resolutionMin = "100x100";
        String resolutionMax = "5000x5000";
        String uri = "";

        Pattern r = Pattern.compile(pattern);
        Pattern b = Pattern.compile(bandwidthPattern);
        Pattern reso = Pattern.compile(resolutionPattern);

        Matcher m = r.matcher(res);

        boolean out = false;


        if (urlVideoType.equalsIgnoreCase("Bandwidth")) {
            uri = getBandwidthUrl(pattern, bandwidthPattern, resolutionPattern, res, bandwidth, bandSelected);
        } else {
            uri = getResolutionUrl(pattern, bandwidthPattern, resolutionPattern, res, resolution, bandwidth, bandSelected, resolSelected);
        }

        return uri;

    }


    public List<DataFragment> extractVideoUrl(String playlistUrl) {

        String pattern = "EXTINF:(\\d?\\d*\\.*\\d*).*\\n(#.*:.*\\n)*(.*\\.ts(\\?.*\\n*)*)";//"EXTINF:(\\d?\\d*\\.\\d*).*\\n(#.*:.*\\n)*(.*\\.ts)";
        final List<DataFragment> mediaList = new ArrayList<>();
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(playlistUrl);
        while (m.find()) {
            DataFragment data = new DataFragment(m.group(1), m.group(3));
            mediaList.add(data);
        }
        return mediaList;

    }

    public String getBandwidthUrl(String pattern, String bandwidthPattern, String resolutionPattern, String res, String bandwidth, String bandSelected) {
        String bandwidthMax = "100000000";
        String secBandwidth = " ";
        String urlCandidate = "";
        Pattern r = Pattern.compile(pattern);
        Pattern b = Pattern.compile(bandwidthPattern);
        Pattern reso = Pattern.compile(resolutionPattern);
        Matcher m = r.matcher(res);


        while (m.find()) {
            Matcher mb = b.matcher(m.group(1));
            //Matcher mBandwidth = b.matcher(m.toString());
            Matcher mResolution = reso.matcher(m.toString());

            mb.find();
            if ((bandSelected.equalsIgnoreCase("customBandwidth")) && (Integer.parseInt(mb.group(1)) <= Integer.parseInt(bandwidth)) && mResolution.find()) {
                if ((Integer.parseInt(mb.group(1)) == Integer.parseInt(bandwidth)) || selectBandwidth(bandwidth, mb.group(1)))
                    urlCandidate = m.group(2);
            } else if ((bandSelected.equalsIgnoreCase("minBandwidth")) && mResolution.find()) {
                if (secBandwidth.equals(" ") && selectBandwidth(mb.group(1), bandwidthMax) || (!secBandwidth.equals(" ") && selectBandwidth(mb.group(1), secBandwidth))) {
                    secBandwidth = mb.group(1);
                    urlCandidate = m.group(2);
                }
            } else if ((bandSelected.equalsIgnoreCase("maxBandwidth"))) {
                if (secBandwidth.equals(" ") && !selectBandwidth(bandwidthMax, mb.group(1)) || (!secBandwidth.equals(" ") && selectBandwidth(secBandwidth, mb.group(1)))) {
                    secBandwidth = mb.group(1);
                    urlCandidate = m.group(2);
                }
            }

        }
        return urlCandidate;

    }

    public String getResolutionUrl(String pattern, String bandwidthPattern, String resolutionPattern, String res, String resolution, String bandwidth, String bandSelected, String resolSelected) {
        String bandwidthMax = "100000000";
        String secBandwidth = " ";
        String secResolution = " ";
        String resolutionMin = "100x100";
        String resolutionMax = "5000x5000";
        String uri = "";
        Pattern r = Pattern.compile(pattern);

        Pattern b = Pattern.compile(bandwidthPattern);
        Pattern reso = Pattern.compile(resolutionPattern);
        Matcher m = r.matcher(res);
        boolean out = false;
        while (m.find() && !out) {

            Matcher mreso = reso.matcher(m.group(1));
            mreso.find();
            Matcher mb = b.matcher(m.group(1));
            mb.find();


            Matcher mResolution = reso.matcher(m.toString());
            Matcher mBandwidth = b.matcher(m.toString());

            if (resolSelected.equalsIgnoreCase("customResolution") && mResolution.find()) {
                if (bandSelected.equalsIgnoreCase("customBandwidth")) {

                    if (mreso.group(1).equals(resolution)) {
                        uri = m.group(2);
                        out = true;
                    } else {
//                            if ((secResolution.equals(" ") && smaller(resolution, mreso.group(1))) || (!secResolution.equals(" ") && findResolution(resolution, secResolution, mreso.group(1)))) {
                        if ((findResolution(resolution, secResolution, mreso.group(1)))) {
                            secResolution = mreso.group(1);
                            uri = m.group(2);

                            if ((Integer.parseInt(mb.group(1)) == Integer.parseInt(bandwidth)) || selectBandwidth(bandwidth, mb.group(1))) {
                                secBandwidth = mb.group(1);
                                uri = m.group(2);
                            }
                        }

                    }

                } else if (bandSelected.equalsIgnoreCase("minBandwidth")) {
                    if ((secResolution.equals(" ") && smaller(resolution, mreso.group(1))) || (!secResolution.equals(" ") && findResolution(resolution, secResolution, mreso.group(1)))) {
                        secResolution = mreso.group(1);
                    }
                    if (secBandwidth.equals(" ") && selectBandwidth(mb.group(1), bandwidthMax) || (!secBandwidth.equals(" ") && selectBandwidth(mb.group(1), secBandwidth))) {
                        secBandwidth = mb.group(1);
                        uri = m.group(2);
                    }

                } else if (bandSelected.equalsIgnoreCase("maxBandwidth")) {
                    if ((secResolution.equals(" ") && smaller(resolution, mreso.group(1))) || (!secResolution.equals(" ") && findResolution(resolution, secResolution, mreso.group(1)))) {
                        secResolution = mreso.group(1);
                    }
                    if (secBandwidth.equals(" ") && !selectBandwidth(bandwidthMax, mb.group(1)) || (!secBandwidth.equals(" ") && selectBandwidth(secBandwidth, mb.group(1)))) {
                        secBandwidth = mb.group(1);
                        uri = m.group(2);
                    }
                }

            } else if (resolSelected.equalsIgnoreCase("minResolution") && mResolution.find()) {

                if (bandSelected.equalsIgnoreCase("customBandwidth")) {

                    if ((secResolution.equals(" ") && smaller(resolutionMax, mreso.group(1))) || (!secResolution.equals(" ") && smaller(secResolution, mreso.group(1)))) {
                        secResolution = mreso.group(1);
                        secBandwidth = mb.group(1);
                        uri = m.group(2);
                    } else if (secResolution.equals(mreso.group(1))) {
                        if ((Integer.parseInt(mb.group(1)) == Integer.parseInt(bandwidth)) || selectBandwidth(mb.group(1), secBandwidth))
                            secBandwidth = mb.group(1);
                        uri = m.group(2);
                    }


                } else if (bandSelected.equalsIgnoreCase("minBandwidth")) {

                    if ((secResolution.equals(" ") && smaller(resolutionMax, mreso.group(1))) || (!secResolution.equals(" ") && smaller(secResolution, mreso.group(1)))) {
                        secResolution = mreso.group(1);

                        if (secBandwidth.equals(" ") && selectBandwidth(mb.group(1), bandwidthMax) || (!secBandwidth.equals(" ") && selectBandwidth(mb.group(1), secBandwidth))) {
                            secBandwidth = mb.group(1);
                            uri = m.group(2);
                        }
                    }

                } else if (bandSelected.equalsIgnoreCase("maxBandwidth")) {
                    if ((secResolution.equals(" ") && smaller(resolutionMax, mreso.group(1))) || (!secResolution.equals(" ") && smaller(secResolution, mreso.group(1)))) {
                        secResolution = mreso.group(1);

                        if (secBandwidth.equals(" ") && !selectBandwidth(bandwidthMax, mb.group(1)) || (!secBandwidth.equals(" ") && selectBandwidth(secBandwidth, mb.group(1)))) {
                            secBandwidth = mb.group(1);
                            uri = m.group(2);
                        }
                    }
                }
            } else if (resolSelected.equalsIgnoreCase("maxResolution") && mResolution.find()) {
                if (bandSelected.equalsIgnoreCase("customBandwidth")) {
                    if ((Integer.parseInt(mb.group(1)) == Integer.parseInt(bandwidth)) || selectBandwidth(bandwidth, mb.group(1))) {
                        if ((secResolution.equals(" ") && smaller(resolutionMax, mreso.group(1))) || (!secResolution.equals(" ") && smaller(secResolution, mreso.group(1)))) {
                            secResolution = mreso.group(1);
                            uri = m.group(2);

                        }
                    }

                } else if (bandSelected.equalsIgnoreCase("minBandwidth")) {
                    if ((secResolution.equals(" ") && smaller(resolutionMax, mreso.group(1))) || (!secResolution.equals(" ") && smaller(secResolution, mreso.group(1)))) {
                        secResolution = mreso.group(1);

                        if (secBandwidth.equals(" ") && selectBandwidth(mb.group(1), bandwidthMax) || (!secBandwidth.equals(" ") && selectBandwidth(mb.group(1), secBandwidth))) {
                            secBandwidth = mb.group(1);
                            uri = m.group(2);
                        }
                    }
                } else if (bandSelected.equalsIgnoreCase("maxBandwidth")) {

                    if ((secResolution.equals(" ") && smaller(resolutionMax, mreso.group(1))) || (!secResolution.equals(" ") && smaller(secResolution, mreso.group(1)))) {
                        secResolution = mreso.group(1);

                        if (secBandwidth.equals(" ") && !selectBandwidth(bandwidthMax, mb.group(1)) || (!secBandwidth.equals(" ") && selectBandwidth(secBandwidth, mb.group(1)))) {
                            secBandwidth = mb.group(1);
                            uri = m.group(2);
                        }
                    }
                }
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


    //Checks if the resolution of the video (candidate2) is closer to the resolution custom (target) than the one already saved (candidate1)
    public boolean findResolution(String target, String candidate1, String candidate2) {
        boolean ret = false;

        if (candidate1.trim().equals("")) {
            return true;
        } else {
            String[] targetWords = target.split("x");
            String[] candidate1Words = candidate1.split("x");
            String[] candidate2Words = candidate2.split("x");

            int difT21 = Integer.parseInt(targetWords[0]) - Integer.parseInt(candidate2Words[0]);
            int difT22 = Integer.parseInt(targetWords[1]) - Integer.parseInt(candidate2Words[1]);

            int difT11 = Integer.parseInt(targetWords[0]) - Integer.parseInt(candidate1Words[0]);
            int difT12 = Integer.parseInt(targetWords[1]) - Integer.parseInt(candidate1Words[1]);

            if (difT21 < 0)
                difT21 = difT21 * -1;

            if (difT22 < 0)
                difT22 = difT22 * -1;

            if (!(difT11 < difT21)) {
                if (difT11 == difT21) {
                    ret = difT12 > difT22;
                } else {
                    ret = true;
                }
            }


            return ret;
        }
    }


    //Compare if the resolution that comes (candidate1) is smaller than the one entered (target)
    public boolean smaller(String target, String candidate1) {
        String[] targetWords = target.split("x");
        String[] candidate1Words = candidate1.split("x");
        int difT11 = Integer.parseInt(targetWords[0]) - Integer.parseInt(candidate1Words[0]);
        int difT12 = Integer.parseInt(targetWords[1]) - Integer.parseInt(candidate1Words[1]);
        return (!((difT11 < 0) || (difT12 < 0)));
    }


    public boolean selectBandwidth(String target, String candidate) {
        int minDiff = Integer.parseInt(candidate);
        int diff = Integer.parseInt(target) - Integer.parseInt(candidate);
        return (diff < minDiff);
    }


    protected boolean isSuccessCode(int code) {
        return code >= 200 && code <= 399;
    }


}
