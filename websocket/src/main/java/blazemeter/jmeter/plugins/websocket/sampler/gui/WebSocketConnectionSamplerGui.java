
package blazemeter.jmeter.plugins.websocket.sampler.gui;

import java.awt.BorderLayout;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import blazemeter.jmeter.plugins.websocket.sampler.WebSocketConnectionSampler;


public class WebSocketConnectionSamplerGui extends AbstractSamplerGui {

    private WebSocketConnectionSamplerPanel webSocketConnectionSamplerPanel;

    public WebSocketConnectionSamplerGui() {
        super();
        init();
        initFields();

        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);
        add(webSocketConnectionSamplerPanel, BorderLayout.CENTER);
    }

    @Override
    public String getStaticLabel() {
        return "WebSocket Connection Sampler DEPRECATED";
    }

    @Override
    public String getLabelResource() {
        throw new IllegalStateException("This shouldn't be called"); //$NON-NLS-1$
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (element instanceof WebSocketConnectionSampler) {
            WebSocketConnectionSampler WebSocketConnectionSamplerTestElement = (WebSocketConnectionSampler) element;
            webSocketConnectionSamplerPanel.setServer(WebSocketConnectionSamplerTestElement.getServer());
            webSocketConnectionSamplerPanel.setPort(Integer.toString(WebSocketConnectionSamplerTestElement.getPort()));
            webSocketConnectionSamplerPanel.setConnectionTimeout(WebSocketConnectionSamplerTestElement.getConnectionTimeout());
            webSocketConnectionSamplerPanel.setProtocol(WebSocketConnectionSamplerTestElement.getProtocol());
            webSocketConnectionSamplerPanel.setPath(WebSocketConnectionSamplerTestElement.getPath());
            webSocketConnectionSamplerPanel.setContentEncoding(WebSocketConnectionSamplerTestElement.getContentEncoding());
            webSocketConnectionSamplerPanel.setConnectionId(WebSocketConnectionSamplerTestElement.getConnectionId());
            webSocketConnectionSamplerPanel.setImplementation(WebSocketConnectionSamplerTestElement.getImplementation());
            webSocketConnectionSamplerPanel.setCloseConnectionPattern(WebSocketConnectionSamplerTestElement.getCloseConnectionPattern());

            Arguments queryStringParameters = WebSocketConnectionSamplerTestElement.getQueryStringParameters();
            if (queryStringParameters != null) {
                webSocketConnectionSamplerPanel.getAttributePanel().configure(queryStringParameters);
            }
            
            Arguments queryStringPatterns = WebSocketConnectionSamplerTestElement.getQueryStringPatterns();
            if (queryStringPatterns != null) {
                webSocketConnectionSamplerPanel.getPatternsPanel().configure(queryStringParameters);
            }
        }
    }

    @Override
    public TestElement createTestElement() {
        WebSocketConnectionSampler preproc = new WebSocketConnectionSampler();
        configureTestElement(preproc);
        return preproc;
    }

    @Override
    public void modifyTestElement(TestElement te) {
        configureTestElement(te);
        if (te instanceof WebSocketConnectionSampler) {
            WebSocketConnectionSampler WebSocketConnectionSamplerTestElement = (WebSocketConnectionSampler) te;
            WebSocketConnectionSamplerTestElement.setServer(webSocketConnectionSamplerPanel.getServer());
            WebSocketConnectionSamplerTestElement.setPort(webSocketConnectionSamplerPanel.getPort());
            WebSocketConnectionSamplerTestElement.setConnectionTimeout(webSocketConnectionSamplerPanel.getConnectionTimeout());
            WebSocketConnectionSamplerTestElement.setProtocol(webSocketConnectionSamplerPanel.getProtocol());
            WebSocketConnectionSamplerTestElement.setPath(webSocketConnectionSamplerPanel.getPath());
            WebSocketConnectionSamplerTestElement.setContentEncoding(webSocketConnectionSamplerPanel.getContentEncoding());
            WebSocketConnectionSamplerTestElement.setConnectionId(webSocketConnectionSamplerPanel.getConnectionId());
            WebSocketConnectionSamplerTestElement.setImplementation(webSocketConnectionSamplerPanel.getImplementation());
            WebSocketConnectionSamplerTestElement.setCloseConnectionPattern(webSocketConnectionSamplerPanel.getCloseConnectionPattern());


            ArgumentsPanel queryStringParameters = webSocketConnectionSamplerPanel.getAttributePanel();
            if (queryStringParameters != null) {
            	WebSocketConnectionSamplerTestElement.setQueryStringParameters((Arguments)queryStringParameters.createTestElement());
            }
            
            ArgumentsPanel queryStringPatterns = webSocketConnectionSamplerPanel.getPatternsPanel();
            if (queryStringPatterns != null) {
            	WebSocketConnectionSamplerTestElement.setQueryStringPatterns((Arguments)queryStringPatterns.createTestElement());
            }
        }
    }

    @Override
    public void clearGui() {
        super.clearGui();
        initFields();
    }

    private void init() {
        webSocketConnectionSamplerPanel = new WebSocketConnectionSamplerPanel();
    }

    private void initFields() {
        webSocketConnectionSamplerPanel.initFields();
    }
}
