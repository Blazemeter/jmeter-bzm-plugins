
package blazemeter.jmeter.plugins.websocket.sampler.gui;

import java.awt.BorderLayout;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

//import blazemeter.jmeter.plugins.websocket.sampler.WebSocketConnectionSampler;


public class WebSocketConnectionConfigGui extends AbstractConfigGui {

    private WebSocketConnectionConfigPanel webSocketConnectionConfigPanel;

    public WebSocketConnectionConfigGui() {
        super();
        init();
        initFields();

        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);
        add(webSocketConnectionConfigPanel, BorderLayout.CENTER);
    }

    @Override
    public String getStaticLabel() {
        return "WebSocket/MQTT Connection Config";
    }
    
    @Override
    public String getLabelResource() {
        throw new IllegalStateException("This shouldn't be called"); //$NON-NLS-1$
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (element instanceof ConfigTestElement) {
        	ConfigTestElement configTestElement = (ConfigTestElement) element;
            webSocketConnectionConfigPanel.setServer(configTestElement.getPropertyAsString("WebSocketConnectionConfig.Server"));
            webSocketConnectionConfigPanel.setPort(configTestElement.getPropertyAsString("WebSocketConnectionConfig.Port"));
            webSocketConnectionConfigPanel.setConnectionTimeout(configTestElement.getPropertyAsString("WebSocketConnectionConfig.ConnectionTimeout"));
            webSocketConnectionConfigPanel.setProtocol(configTestElement.getPropertyAsString("WebSocketConnectionConfig.Protocol"));
            webSocketConnectionConfigPanel.setPath(configTestElement.getPropertyAsString("WebSocketConnectionConfig.Path"));
            webSocketConnectionConfigPanel.setTopic(configTestElement.getPropertyAsString("WebSocketConnectionConfig.Topic"));
            webSocketConnectionConfigPanel.setContentEncoding(configTestElement.getPropertyAsString("WebSocketConnectionConfig.Encoding"));
            webSocketConnectionConfigPanel.setImplementation(configTestElement.getPropertyAsString("WebSocketConnectionConfig.Implementation"));
            webSocketConnectionConfigPanel.setCloseConnectionPattern(configTestElement.getPropertyAsString("WebSocketConnectionConfig.CloseConnectionPattern"));
            webSocketConnectionConfigPanel.setProtocolWSMQTTComboBox(configTestElement.getPropertyAsString("WebSocketConnectionConfig.ProtocolWSMQTTComboBox"));
            webSocketConnectionConfigPanel.setLogLevel(configTestElement.getPropertyAsString("WebSocketConnectionConfig.LogLevel"));
           
            Arguments queryStringParameters = (Arguments) configTestElement.getProperty("WebSocketConnectionConfig.HTTPRequest.ARGUMENTS").getObjectValue();
            if (queryStringParameters != null) {
                webSocketConnectionConfigPanel.getAttributePanel().configure(queryStringParameters);
            }
            
            Arguments queryStringPatterns = (Arguments) configTestElement.getProperty("WebSocketConnectionConfig.ResponsePatterns.ARGUMENTS").getObjectValue();
            if (queryStringPatterns != null) {
                webSocketConnectionConfigPanel.getPatternsPanel().configure(queryStringPatterns);
            }
        }
    }

    @Override
    public TestElement createTestElement() {
    	ConfigTestElement config = new ConfigTestElement();
    	config.setName(this.getName());
    	config.setProperty(TestElement.GUI_CLASS, this.getClass().getName());
    	config.setProperty(TestElement.TEST_CLASS, config.getClass().getName());
        modifyTestElement(config);
        return config;
    }

    @Override
    public void modifyTestElement(TestElement te) {
        configureTestElement(te);
        if (te instanceof ConfigTestElement) {
        	ConfigTestElement configTestElement = (ConfigTestElement) te;
        	configTestElement.setProperty("WebSocketConnectionConfig.Server", webSocketConnectionConfigPanel.getServer());
        	configTestElement.setProperty("WebSocketConnectionConfig.Port", webSocketConnectionConfigPanel.getPort());
        	configTestElement.setProperty("WebSocketConnectionConfig.ConnectionTimeout", webSocketConnectionConfigPanel.getConnectionTimeout());
        	configTestElement.setProperty("WebSocketConnectionConfig.Protocol", webSocketConnectionConfigPanel.getProtocolSelected());
        	configTestElement.setProperty("WebSocketConnectionConfig.Path", webSocketConnectionConfigPanel.getPath());
        	configTestElement.setProperty("WebSocketConnectionConfig.Topic", webSocketConnectionConfigPanel.getTopic());
        	configTestElement.setProperty("WebSocketConnectionConfig.Encoding", webSocketConnectionConfigPanel.getContentEncoding());
        	configTestElement.setProperty("WebSocketConnectionConfig.Implementation", webSocketConnectionConfigPanel.getImplementation());
            configTestElement.setProperty("WebSocketConnectionConfig.CloseConnectionPattern", webSocketConnectionConfigPanel.getCloseConnectionPattern());
            configTestElement.setProperty("WebSocketConnectionConfig.ProtocolWSMQTTComboBox", webSocketConnectionConfigPanel.getProtocolWSMQTTComboBox());
            configTestElement.setProperty("WebSocketConnectionConfig.LogLevel", webSocketConnectionConfigPanel.getLogLevel());
            
            
            HTTPArgumentsPanel queryStringParameters = webSocketConnectionConfigPanel.getAttributePanel();
            if (queryStringParameters != null) {
            	configTestElement.setProperty(new TestElementProperty("WebSocketConnectionConfig.HTTPRequest.ARGUMENTS", (Arguments)queryStringParameters.createTestElement()));
            }
            
            ArgumentsPanel queryStringPatterns = webSocketConnectionConfigPanel.getPatternsPanel();
            if (queryStringPatterns != null) {
            	configTestElement.setProperty(new TestElementProperty("WebSocketConnectionConfig.ResponsePatterns.ARGUMENTS", (Arguments)queryStringPatterns.createTestElement()));
            }
        }
    }

    @Override
    public void clearGui() {
        super.clearGui();
        initFields();
    }

    private void init() {
        webSocketConnectionConfigPanel = new WebSocketConnectionConfigPanel();
    }

    private void initFields() {
        webSocketConnectionConfigPanel.initFields();
    }
}
