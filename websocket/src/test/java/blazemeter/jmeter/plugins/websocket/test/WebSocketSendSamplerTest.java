package blazemeter.jmeter.plugins.websocket.test;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import blazemeter.jmeter.plugins.websocket.sampler.Factory;
import blazemeter.jmeter.plugins.websocket.sampler.MqttCallBackImpl;
import blazemeter.jmeter.plugins.websocket.sampler.WebSocketConnection;
import blazemeter.jmeter.plugins.websocket.sampler.WebSocketSendSampler;
import kg.apc.emulators.TestJMeterUtils;



public class WebSocketSendSamplerTest {
 
	private static Factory factoryMock;
	private static MqttCallBackImpl handlerMqttMock;
	private static IMqttAsyncClient mqttAsyncClientMock;
	
	@BeforeClass
    public static void setUpClass()
            throws Exception {
		TestJMeterUtils.createJmeterEnv();
		factoryMock = Mockito.mock(Factory.class);
		handlerMqttMock = Mockito.mock(MqttCallBackImpl.class);
		mqttAsyncClientMock = Mockito.mock(IMqttAsyncClient.class);
		JMeterUtils.setProperty("WebSocketConnectionConfig.Server", "server");
    }
	
//	@Test
//    public void testSample() throws Exception{
//		
//		Mockito.when(factoryMock.getMqttHandler(Mockito.any(IMqttAsyncClient.class), Mockito.any(String.class), 
//				Mockito.any(String.class), Mockito.any(String.class)))
//				.thenReturn(handlerMqttMock);
//		
//		Mockito.when(factoryMock.getMqttAsyncClient(Mockito.any(String.class), Mockito.any(String.class), 
//				Mockito.any(String.class), Mockito.any(MemoryPersistence.class)))
//				.thenReturn(mqttAsyncClientMock);
//		
//		Mockito.when(mqttAsyncClientMock.isConnected()).thenReturn(false)
//														.thenReturn(true);
//		
//		Mockito.when(handlerMqttMock.isConnected()).thenReturn(true);
//		
//		WebSocketSendSampler webSocketSendSampler = new WebSocketSendSampler();
//		webSocketSendSampler.setFactory(factoryMock);
//		webSocketSendSampler.setThreadName("Mock");
//		webSocketSendSampler.setProperty("WebSocketConnectionConfig.Server", "server");
//		webSocketSendSampler.setProperty("WebSocketConnectionConfig.Path", "path");
//		webSocketSendSampler.setProperty("WebSocketConnectionConfig.Port", "80");
//		webSocketSendSampler.setProperty("WebSocketConnectionConfig.ProtocolWSMQTTComboBox", "Mqtt");
//		webSocketSendSampler.setProperty("WebSocketConnectionConfig.Protocol", "tcp");
//		webSocketSendSampler.setProperty("payloadContent", "payload");
//		
//		
//		
//		webSocketSendSampler.testStarted();
//		
//		SampleResult res = webSocketSendSampler.sample(null);
//	   
//       
//    }	
//	
	@Test
    public void testGetUri() throws Exception{
		
		WebSocketSendSampler webSocketSendSampler = new WebSocketSendSampler();
		webSocketSendSampler.setFactory(factoryMock);
		webSocketSendSampler.setThreadName("Mock");
		webSocketSendSampler.setProperty("WebSocketConnectionConfig.Server", "server");
		webSocketSendSampler.setProperty("WebSocketConnectionConfig.Path", "path");
		webSocketSendSampler.setProperty("WebSocketConnectionConfig.Port", "810");
		webSocketSendSampler.setProperty("WebSocketConnectionConfig.ProtocolWSMQTTComboBox", "Mqtt");
		webSocketSendSampler.setProperty("WebSocketConnectionConfig.Protocol", "tcp");
		webSocketSendSampler.setProperty("payloadContent", "payload");
		
		
		URI uri = webSocketSendSampler.getUri(); 
       
    }	
}
