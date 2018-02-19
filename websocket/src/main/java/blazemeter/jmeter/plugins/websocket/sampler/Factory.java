package blazemeter.jmeter.plugins.websocket.sampler;

import java.io.Serializable;

import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import io.inventit.dev.mqtt.paho.MqttWebSocketAsyncClient;

public class Factory implements Serializable{

	public MqttCallBackImpl getMqttHandler (IMqttAsyncClient client, String clientID, String logLevel, String encoding){
		return new MqttCallBackImpl(client, clientID, logLevel, encoding);
	}
	
	public WebSocketConnection getWebSocketHandler (WebSocketClient client, String closeConnectionPattern, String encoding){
		return new WebSocketConnection(client, closeConnectionPattern, encoding);
	}
	
	public IMqttAsyncClient getMqttAsyncClient (String protocol, String mqttUrl, String clientID, MemoryPersistence persistence) throws MqttException {
		if(protocol.equals("tcp"))
			return new MqttAsyncClient(mqttUrl, clientID, persistence);
		else if (protocol.equals("ws") || protocol.equals("wss"))
			return new MqttWebSocketAsyncClient (mqttUrl, clientID, persistence);
		else
			return null;
	}
}
