package com.blazemeter.api.explorer;

import kg.apc.jmeter.http.HttpUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Base entity for BlazeMeter explorer classes
 */
public class BZAObject {

    protected static final Logger log = LoggingManager.getLoggerForClass();

    protected String id;
    protected String name;
    protected HttpUtils httpUtils;

    public BZAObject(HttpUtils httpUtils, String id, String name) {
        this.httpUtils = httpUtils;
        this.id = id;
        this.name = name;
    }

    public HttpUtils getHttpUtils() {
        return httpUtils;
    }

    public void setHttpUtils(HttpUtils httpUtils) {
        this.httpUtils = httpUtils;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
