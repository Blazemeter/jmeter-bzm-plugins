
package blazemeter.jmeter.plugins.websocket.sampler.gui;

import java.awt.BorderLayout;

import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import blazemeter.jmeter.plugins.websocket.sampler.WebSocketCollectorSampler;




public class WebSocketCollectorSamplerGui extends AbstractSamplerGui {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5507578136184456991L;
	private WebSocketCollectorSamplerPanel WebSocketCollectorSamplerPanel;

    public WebSocketCollectorSamplerGui() {
        super();
        init();
        initFields();

        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);
        add(WebSocketCollectorSamplerPanel, BorderLayout.CENTER);
    }

    @Override
    public String getStaticLabel() {
        return "WebSocket Collector Sampler";
    }

    @Override
    public String getLabelResource() {
        throw new IllegalStateException("This shouldn't be called"); //$NON-NLS-1$
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (element instanceof WebSocketCollectorSampler) {
            WebSocketCollectorSampler WebSocketCollectorSamplerTestElement = (WebSocketCollectorSampler) element; 
            WebSocketCollectorSamplerPanel.setWaitUntilMessage(WebSocketCollectorSamplerTestElement.getWaitUntilMessage()); 
            WebSocketCollectorSamplerPanel.setCloseConnection(WebSocketCollectorSamplerTestElement.getCloseConnection()); 
            WebSocketCollectorSamplerPanel.setResponsePattern(WebSocketCollectorSamplerTestElement.getResponsePattern()); 
            WebSocketCollectorSamplerPanel.setResponseTimeout(WebSocketCollectorSamplerTestElement.getResponseTimeout()); 
        }
    }

    @Override
    public TestElement createTestElement() {
        WebSocketCollectorSampler preproc = new WebSocketCollectorSampler();
        configureTestElement(preproc);
        return preproc;
    }

    @Override
    public void modifyTestElement(TestElement te) {
        configureTestElement(te);
        if (te instanceof WebSocketCollectorSampler) {
            WebSocketCollectorSampler WebSocketCollectorSamplerTestElement = (WebSocketCollectorSampler) te;
            WebSocketCollectorSamplerTestElement.setWaitUntilMessage(WebSocketCollectorSamplerPanel.getWaitUntilResponse());
            WebSocketCollectorSamplerTestElement.setCloseConnection(WebSocketCollectorSamplerPanel.getCloseConnection());
            WebSocketCollectorSamplerTestElement.setResponsePattern(WebSocketCollectorSamplerPanel.getResponsePattern());
            WebSocketCollectorSamplerTestElement.setResponseTimeout(WebSocketCollectorSamplerPanel.getResponseTimeout());
        }
    }

    @Override
    public void clearGui() {
        super.clearGui();
        initFields();
    }

    private void init() {
        WebSocketCollectorSamplerPanel = new WebSocketCollectorSamplerPanel();
    }

    private void initFields() {
        WebSocketCollectorSamplerPanel.initFields();
    }
}
