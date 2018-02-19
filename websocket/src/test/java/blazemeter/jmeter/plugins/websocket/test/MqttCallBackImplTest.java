package blazemeter.jmeter.plugins.websocket.test;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.jmeter.samplers.SampleResult;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import blazemeter.jmeter.plugins.websocket.sampler.Handler;
import blazemeter.jmeter.plugins.websocket.sampler.MqttCallBackImpl;


public class MqttCallBackImplTest {

	private static IMqttAsyncClient clientMock;
	private static MqttMessage messageMock;
	private static IMqttDeliveryToken tokenMock;
	private static Throwable causeMock;
	
	@BeforeClass
    public static void setUpClass()
            throws Exception {
		clientMock = Mockito.mock(IMqttAsyncClient.class);
		messageMock = Mockito.mock(MqttMessage.class);
		tokenMock = Mockito.mock(IMqttDeliveryToken.class);
		causeMock = Mockito.mock(Throwable.class);
    }
	
	@Test
    public void test1() throws Exception{
		
		
		
		MqttCallBackImpl mqtt = new MqttCallBackImpl (clientMock, "clientID", "info" , "utf-8");
		
		Mockito.when(messageMock.toString()).thenReturn("message2");
		Mockito.when(tokenMock.toString()).thenReturn("token");
		Mockito.when(causeMock.toString()).thenReturn("cause");
		Mockito.when(clientMock.isConnected()).thenReturn(true);
        
		mqtt.addMessage("message1");
		mqtt.messageArrived("topic1", messageMock);
		mqtt.deliveryComplete(tokenMock);
		mqtt.close();
		mqtt.connectionLost(causeMock);
		
		
		String messages = mqtt.getMessages();
		String expectedMessage = "message1\\n"
				+ "\\[RECIVED at (\\d+?)\\] -- clientID -- TOPIC: topic1 NEW MESSAGE ARRIVED: message2\\n"
				+ "CLIENT DISCONNECTION\\n"
				+ "CLIENT CLOSING\\n"
				+ "-- clientID-- CONNECTION LOST: cause\\n";
		
	
		Assert.assertTrue(Pattern.matches(expectedMessage,messages));   
    }	
	
	@Test
    public void test2() throws Exception{
		
		MqttCallBackImpl mqtt = new MqttCallBackImpl (clientMock, "clientID", "debug" , "utf-8");
		
		Mockito.when(messageMock.toString()).thenReturn("message2");
		Mockito.when(tokenMock.toString()).thenReturn("token");
		Mockito.when(causeMock.toString()).thenReturn("cause");
		Mockito.when(clientMock.isConnected()).thenReturn(true);
        
		mqtt.addMessage("message1");
		mqtt.messageArrived("topic1", messageMock);
		mqtt.deliveryComplete(tokenMock);
		mqtt.close();
		
		
		String messages = mqtt.getMessages();
		String expectedMessage = "message1\\n"
				+ "\\[RECIVED at (\\d+?)\\] -- clientID -- TOPIC: topic1 NEW MESSAGE ARRIVED: message2\\n"
				+ "CLIENT DISCONNECTION\\n"
				+ "CLIENT CLOSING\\n";
		
		
		System.out.println(expectedMessage);
		System.out.println(messages);
		
		Assert.assertTrue(Pattern.matches(expectedMessage,messages));
        
        
        
    }	
}
