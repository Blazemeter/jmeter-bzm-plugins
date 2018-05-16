package org.apache.jmeter.protocol.http.control;

import org.apache.jmeter.testelement.property.PropertyIterator;

public class ThreadSafeCookieManager extends CookieManager {


    public ThreadSafeCookieManager() {
        this(new CookieManager());
    }

    public ThreadSafeCookieManager(CookieManager manager) {
        PropertyIterator iter = manager.propertyIterator();
        while (iter.hasNext()) {
            setProperty(iter.next().clone());
        }
        setRunningVersion(manager.isRunningVersion());
        testStarted();
    }

    @Override
    public synchronized void add(Cookie c) {
        super.add(c);
    }

    @Override
    synchronized void removeMatchingCookies(Cookie newCookie) {
        super.removeMatchingCookies(newCookie);
    }

    @Override
    public Object clone() {
        return super.clone();
    }
}
