package com.blazemeter.jmeter.xmpp.actions;

import com.blazemeter.jmeter.xmpp.JMeterXMPPSampler;
import org.apache.jmeter.samplers.SampleResult;
import org.jivesoftware.smack.AbstractXMPPConnection;

import javax.swing.*;
import java.awt.*;

public class Disconnect extends AbstractXMPPAction {

    @Override
    public String getLabel() {
        return "Disconnect from Server";
    }

    @Override
    public SampleResult perform(JMeterXMPPSampler sampler, SampleResult res) throws Exception {
        if (!sampler.getXMPPConnection().isConnected()) {
            return res;
        }
        AbstractXMPPConnection conn = (AbstractXMPPConnection)sampler.getXMPPConnection();
        conn.disconnect();
        if (sampler.getXMPPConnectionConfig() != null)
            sampler.getXMPPConnectionConfig().resetConnection();
        return res;
    }

    @Override
    public void addUI(JComponent panel, GridBagConstraints labelConstraints, GridBagConstraints editConstraints) {
    }

    @Override
    public void clearGui() {

    }

    @Override
    public void setSamplerProperties(JMeterXMPPSampler sampler) {

    }

    @Override
    public void setGuiFieldsFromSampler(JMeterXMPPSampler sampler) {

    }
}
