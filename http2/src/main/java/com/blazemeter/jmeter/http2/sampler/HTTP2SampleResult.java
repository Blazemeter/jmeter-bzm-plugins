/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.blazemeter.jmeter.http2.sampler;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.SampleResult;
import org.eclipse.jetty.http.HttpFields;

/**
 * This is a specialisation of the SampleResult class for the HTTP protocol.
 *
 */
public class HTTP2SampleResult extends SampleResult {

	private static final long serialVersionUID = 241L;

	/** Set of all HTTP methods, that have no body */
	private static final Set<String> METHODS_WITHOUT_BODY = new HashSet<>(
			Arrays.asList(HTTPConstants.HEAD, HTTPConstants.OPTIONS, HTTPConstants.TRACE));

	private String cookies = ""; // never null

	private static int idCount = 0;

	private int id;

	private int embebedResultsDepth;
	private String method;
	private HttpFields httpFieldsResponse;
	private boolean embebedResults;

	private boolean secondaryRequest;

	private String embeddedUrlRE;

	private boolean isPushed;

	/**
	 * The raw value of the Location: header; may be null. This is supposed to
	 * be an absolute URL: <a href=
	 * "http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.30">RFC2616
	 * sec14.30</a> but is often relative.
	 */
	private String redirectLocation;

	private String queryString = ""; // never null

	protected static final String NON_HTTP_RESPONSE_CODE = "Non HTTP response code";
	protected static final String NON_HTTP_RESPONSE_MESSAGE = "Non HTTP response message";
	protected static final String HTTP2_PENDING_RESPONSE = "Pending";
	protected static final String HTTP2_RESPONSE_RECEIVED = "Received";
	protected static final String HTTP2_RESPONSE_CODE_4 = "Not Found";

	private boolean pendingResponse;

	private String requestId;


	public HTTP2SampleResult() {
		super();
	}

	public HTTP2SampleResult(URL url, String method) {
		super();

		this.setSampleLabel(url.toString()); // May be replaced later
		this.setHTTPMethod(method);
		this.setURL(url);
		this.setPendingResponse(true);
		this.setId(HTTP2SampleResult.getNextId());
		this.setPushed(false);
		this.setEmbebedResultsDepth(1);
		this.setEmbebedResults(false);
		this.setResponseCode(HTTP2_PENDING_RESPONSE);
		this.setResponseMessage(HTTP2_PENDING_RESPONSE);
	}

	/**
	 * Construct a 'parent' result for an already-existing result, essentially
	 * cloning it
	 *
	 * @param res
	 *            existing sample result
	 */
	public HTTP2SampleResult(HTTP2SampleResult res) {
		super(res);
		method = res.method;
		cookies = res.cookies;
		queryString = res.queryString;
		redirectLocation = res.redirectLocation;
	}

	/**
	 * Populates the provided HTTPSampleResult with details from the Exception.
	 * Does not create a new instance, so should not be used directly to add a
	 * subsample.
	 *
	 * @param e
	 *            Exception representing the error.
	 * @param res
	 *            SampleResult to be modified
	 * @return the modified sampling result containing details of the Exception.
	 */
	protected static HTTP2SampleResult errorResult(Throwable e, HTTP2SampleResult res) {
		res.setSampleLabel(res.getSampleLabel());
		res.setDataType(SampleResult.TEXT);
		ByteArrayOutputStream text = new ByteArrayOutputStream(200);
		e.printStackTrace(new PrintStream(text));
		res.setResponseData(text.toByteArray());
		res.setResponseCode(NON_HTTP_RESPONSE_CODE + ": " + e.getClass().getName());
		res.setResponseMessage(NON_HTTP_RESPONSE_MESSAGE + ": " + e.getMessage());
		res.setSuccessful(false);
		res.setPendingResponse(false);
		// res.setMonitor(this.isMonitor()); // TODO see if applies to http2
		return res;
	}

	public void setHTTPMethod(String method) {
		this.method = method;
	}

	public String getHTTPMethod() {
		return method;
	}

	public static synchronized int getNextId() {
		int ret = idCount;
		idCount += 1;
		return ret;
	}

	public boolean isEmbebedResults() {
		return embebedResults;
	}

	public String getEmbeddedUrlRE() {
		return embeddedUrlRE;
	}

	public void setEmbeddedUrlRE(String embeddedUrlRE) {
		this.embeddedUrlRE = embeddedUrlRE;
	}

	public void setEmbebedResults(boolean embebedResults) {
		this.embebedResults = embebedResults;
	}

	public int getEmbebedResultsDepth() {
		return embebedResultsDepth;
	}

	public void setEmbebedResultsDepth(int embebedResultsDepth) {
		this.embebedResultsDepth = embebedResultsDepth;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isPushed() {
		return isPushed;
	}

	public void setPushed(boolean pushed) {
		isPushed = pushed;
	}

	public HttpFields getHttpFieldsResponse() {
		return httpFieldsResponse;
	}

	public void setHttpFieldsResponse(HttpFields httpFieldsResponse) {
		this.httpFieldsResponse = new HttpFields(httpFieldsResponse);
	}

	public void setRedirectLocation(String redirectLocation) {
		this.redirectLocation = redirectLocation;
	}

	public String getRedirectLocation() {
		return redirectLocation;
	}

	/**
	 * Determine whether this result is a redirect. Returns true for:
	 * 301,302,303 and 307(GET or HEAD)
	 * 
	 * @return true iff res is an HTTP redirect response
	 */
	public boolean isRedirect() {
		/*
		 * Don't redirect the following: 300 = Multiple choice 304 = Not
		 * Modified 305 = Use Proxy 306 = (Unused)
		 */
		final String[] REDIRECT_CODES = { HTTPConstants.SC_MOVED_PERMANENTLY, HTTPConstants.SC_MOVED_TEMPORARILY,
				HTTPConstants.SC_SEE_OTHER };
		String code = getResponseCode();
		for (String redirectCode : REDIRECT_CODES) {
			if (redirectCode.equals(code)) {
				return true;
			}
		}
		// http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
		// If the 307 status code is received in response to a request other
		// than GET or HEAD,
		// the user agent MUST NOT automatically redirect the request unless it
		// can be confirmed by the user,
		// since this might change the conditions under which the request was
		// issued.
		// See Bug 54119
		if (HTTPConstants.SC_TEMPORARY_REDIRECT.equals(code)
				&& (HTTPConstants.GET.equals(getHTTPMethod()) || HTTPConstants.HEAD.equals(getHTTPMethod()))) {
			return true;
		}
		return false;
	}

	/**
	 * Overrides version in Sampler data to provide more details
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public String getSamplerData() {
		StringBuilder sb = new StringBuilder();
		sb.append(method);
		URL u = super.getURL();
		if (u != null) {
			sb.append(' ');
			sb.append(u.toString());
			sb.append('\n');
			// Include request body if it can have one
			if (!METHODS_WITHOUT_BODY.contains(method)) {
				sb.append("\n").append(method).append(" data:\n");
				sb.append(queryString);
				sb.append('\n');
			}
			if (cookies.length() > 0) {
				sb.append("\nCookie Data:\n");
				sb.append(cookies);
			} else {
				sb.append("\n[no cookies]");
			}
			sb.append('\n');
		}
		final String sampData = super.getSamplerData();
		if (sampData != null) {
			sb.append(sampData);
		}
		return sb.toString();
	}

	/**
	 * @return cookies as a string
	 */
	public String getCookies() {
		return cookies;
	}

	/**
	 * @param string
	 *            representing the cookies
	 */
	public void setCookies(String string) {
		if (string == null) {
			cookies = "";// $NON-NLS-1$
		} else {
			cookies = string;
		}
	}

	/**
	 * Fetch the query string
	 *
	 * @return the query string
	 */
	public String getQueryString() {
		return queryString;
	}

	/**
	 * Save the query string
	 *
	 * @param string
	 *            the query string
	 */
	public void setQueryString(String string) {
		if (string == null) {
			queryString = "";// $NON-NLS-1$
		} else {
			queryString = string;
		}
	}

	/**
	 * Overrides the method from SampleResult - so the encoding can be extracted
	 * from the Meta content-type if necessary.
	 *
	 * Updates the dataEncoding field if the content-type is found.
	 * 
	 * @param defaultEncoding
	 *            Default encoding used if there is no data encoding
	 * @return the dataEncoding value as a String
	 */
	@Override
	public String getDataEncodingWithDefault(String defaultEncoding) {
		String dataEncodingNoDefault = getDataEncodingNoDefault();
		if (dataEncodingNoDefault != null && dataEncodingNoDefault.length() > 0) {
			return dataEncodingNoDefault;
		}
		return defaultEncoding;
	}

	/**
	 * Overrides the method from SampleResult - so the encoding can be extracted
	 * from the Meta content-type if necessary.
	 *
	 * Updates the dataEncoding field if the content-type is found.
	 *
	 * @return the dataEncoding value as a String
	 */
	@Override
	public String getDataEncodingNoDefault() {
		if (super.getDataEncodingNoDefault() == null && getContentType().startsWith("text/html")) { // $NON-NLS-1$
			byte[] bytes = getResponseData();
			// get the start of the file
			String prefix = new String(bytes, 0, Math.min(bytes.length, 2000), Charset.forName(DEFAULT_HTTP_ENCODING));
			// Preserve original case
			String matchAgainst = prefix.toLowerCase(java.util.Locale.ENGLISH);
			// Extract the content-type if present
			final String METATAG = "<meta http-equiv=\"content-type\" content=\""; // $NON-NLS-1$
			int tagstart = matchAgainst.indexOf(METATAG);
			if (tagstart != -1) {
				tagstart += METATAG.length();
				int tagend = prefix.indexOf('\"', tagstart); // $NON-NLS-1$
				if (tagend != -1) {
					final String ct = prefix.substring(tagstart, tagend);
					setEncodingAndType(ct);// Update the dataEncoding
				}
			}
		}
		return super.getDataEncodingNoDefault();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.samplers.SampleResult#getSearchableTokens()
	 */
	@Override
	public List<String> getSearchableTokens() throws Exception {
		List<String> list = new ArrayList<>(super.getSearchableTokens());
		list.add(getQueryString());
		list.add(getCookies());
		return list;
	}

	public void setPendingResponse(boolean pendingResp) {
		pendingResponse = pendingResp;
	}

	public boolean isPendingResponse() {
		boolean ret = pendingResponse;
		SampleResult[] sons = this.getSubResults();
		int i = 0;
		if (sons.length != 0) {
			while ((i < sons.length) && (!ret)) {
				HTTP2SampleResult h = (HTTP2SampleResult) sons[i];
				i++;
				if (h.isSecondaryRequest())
					ret = ret || (h.isPendingResponse());
			}
		}
		return ret;
	}

	public void setRequestId(String id) {
		requestId = id;
	}

	public String getRequestId() {
		return requestId;
	}


	public boolean isSecondaryRequest() {
		return secondaryRequest;
	}

	public void setSecondaryRequest(boolean secondaryRequest) {
		this.secondaryRequest = secondaryRequest;
	}

}
