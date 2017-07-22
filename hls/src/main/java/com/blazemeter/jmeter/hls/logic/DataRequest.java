package com.blazemeter.jmeter.hls.logic;

import java.util.List;
import java.util.Map;

public class DataRequest {
	public Map<String, List<String>> headers;
	private String response;
	private String responseCode;
	private String responseMessage;
	private String contentType;
	private boolean success;
	private long sentBytes;
	private String contentEncoding;
	private String requestHeaders;

	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	/**
	 * @return Returns the Header.
	 */

	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}

	public String getHeadersAsString() {
		String res = "";
		for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
			res = res + entry.getKey() + " :";
			for (String value : entry.getValue()) {
				res = res + " " + value;
			}
			res = res + "\n";

		}
		return res;

	}

	/**
	 * @return Returns the Response.
	 */
	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	/**
	 * @return Returns the ResponseCode.
	 */
	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * @return Returns the ResponseMessage.
	 */
	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	/**
	 * @return Returns the ContentType.
	 */
	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * @return Returns the Success.
	 */
	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	/**
	 * @return Returns the Sent Bytes.
	 */
	public long getSentBytes() {
		return sentBytes;
	}

	public void setSentBytes(long sentBytes) {
		this.sentBytes = sentBytes;
	}

	/**
	 * @return Returns the Content Encoding.
	 */
	public String getContentEncoding() {
		return contentEncoding;
	}

	public void setContentEncoding(String contentEncoding) {
		this.contentEncoding = contentEncoding;
	}

	/**
	 * @return Returns the Request Headers.
	 */
	public String getRequestHeaders() {
		return requestHeaders;
	}

	public void setRequestHeaders(String setRequestHeaders) {
		this.requestHeaders = setRequestHeaders;
	}
	
	@Override
    public boolean equals(Object obj) {
        if (obj==this) return true;
        if (obj==null || obj.getClass()!=this.getClass()) return false;
        return (this.headers.equals(((DataRequest) obj).headers) &&
        		this.response.equals(((DataRequest) obj).response) &&
        		this.responseCode.equals(((DataRequest) obj).responseCode) &&
        		this.responseMessage.equals(((DataRequest) obj).responseMessage) &&
        		this.contentType.equals(((DataRequest) obj).contentType) &&
        		this.success == ((DataRequest) obj).success &&
        		this.sentBytes == ((DataRequest) obj).sentBytes &&
        		this.contentEncoding.equals(((DataRequest) obj).contentEncoding) &&
        		this.requestHeaders.equals(((DataRequest) obj).requestHeaders));     
    }
	
}
