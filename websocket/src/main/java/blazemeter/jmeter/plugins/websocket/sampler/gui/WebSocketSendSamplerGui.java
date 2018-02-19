
package blazemeter.jmeter.plugins.websocket.sampler.gui;

import java.awt.BorderLayout;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import blazemeter.jmeter.plugins.websocket.sampler.WebSocketSendSampler;


public class WebSocketSendSamplerGui extends AbstractSamplerGui {

    private WebSocketSendSamplerPanel webSocketSendSamplerPanel;

    public WebSocketSendSamplerGui() {
        super();
        init();
        initFields();

        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);
        add(webSocketSendSamplerPanel, BorderLayout.CENTER);
    }

    @Override
    public String getStaticLabel() {
        return "WebSocket Send Sampler";
    }

    @Override
    public String getLabelResource() {
        throw new IllegalStateException("This shouldn't be called"); //$NON-NLS-1$
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (element instanceof WebSocketSendSampler) {
            WebSocketSendSampler WebSocketSendSamplerTestElement = (WebSocketSendSampler) element;   
            webSocketSendSamplerPanel.setPayloadContent(WebSocketSendSamplerTestElement.getPayloadContent());
            webSocketSendSamplerPanel.setWaitUntilResponse(WebSocketSendSamplerTestElement.getWaitUntilResponse());
            webSocketSendSamplerPanel.setResponsePattern(WebSocketSendSamplerTestElement.getResponsePattern());
            webSocketSendSamplerPanel.setResponseTimeout(WebSocketSendSamplerTestElement.getResponseTimeout());
        }
    }

    @Override
    public TestElement createTestElement() {
        WebSocketSendSampler preproc = new WebSocketSendSampler();
        configureTestElement(preproc);
        return preproc;
    }

    @Override
    public void modifyTestElement(TestElement te) {
        configureTestElement(te);
        if (te instanceof WebSocketSendSampler) {
            WebSocketSendSampler WebSocketSendSamplerTestElement = (WebSocketSendSampler) te;
            WebSocketSendSamplerTestElement.setPayloadContent(webSocketSendSamplerPanel.getPayloadContent());
            WebSocketSendSamplerTestElement.setWaitUntilResponse(webSocketSendSamplerPanel.getWaitUntilResponse());
            WebSocketSendSamplerTestElement.setResponsePattern(webSocketSendSamplerPanel.getResponsePattern());
            WebSocketSendSamplerTestElement.setResponseTimeout(webSocketSendSamplerPanel.getResponseTimeout());
        }
    }

    @Override
    public void clearGui() {
        super.clearGui();
        initFields();
    }

    private void init() {
        webSocketSendSamplerPanel = new WebSocketSendSamplerPanel();
    }

    private void initFields() {
        webSocketSendSamplerPanel.initFields();
    }
}
