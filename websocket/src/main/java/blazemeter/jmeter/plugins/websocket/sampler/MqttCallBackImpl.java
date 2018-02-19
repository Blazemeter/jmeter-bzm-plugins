package blazemeter.jmeter.plugins.websocket.sampler;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;



public class MqttCallBackImpl extends Handler implements MqttCallback, Serializable {
	protected static final Logger log = LoggingManager.getLoggerForClass();	
	private String clientID;
	private String logLevel;
	private IMqttAsyncClient  client;
	private String encoding;
	
    public MqttCallBackImpl (IMqttAsyncClient  client,String clientID,String logLevel,String encoding) {
		super(encoding);    	
    	this.clientID = clientID;
		this.logLevel = logLevel;
		this.client = client;
		this.encoding = encoding;
	}
    
    public void close(){
    	if (client.isConnected()){
    		try {
    			client.disconnect();
    			log.info("CLIENT DISCONNECTION");
    			messages.add("CLIENT DISCONNECTION");
    			client.close();
    			log.info("CLIENT CLOSING");
    			messages.add("CLIENT CLOSING");
    		} catch (MqttException e) {
    			e.printStackTrace();
    		}
    	}
    }
    

	@Override
    public void connectionLost(Throwable cause) { 
		
		//Console Log
    	System.out.println("-- "+ clientID + "-- CONNECTION LOST: " + cause.toString());
    	//File Log    
    	log.error("-- "+ clientID + "-- CONNECTION LOST: " + cause.toString());   		
    	messages.add("-- "+ clientID + "-- CONNECTION LOST: " + cause.toString());
    	
    	
    }
	
	public void addMessage(String mess){
    	messages.add(mess);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
    	messages.add("[RECIVED at "+ System.currentTimeMillis() + "] -- "+ clientID + " -- TOPIC: " + topic + " NEW MESSAGE ARRIVED: "+ message);
    	
    	if(logLevel.toLowerCase().equals("info")){
    		 //Console Log
        	System.out.println("--"+ clientID + "-- TOPIC: " + topic + " NEW MESSAGE ARRIVED ");
        	//File Log
        	log.info("--"+ clientID + "-- TOPIC: " + topic + " NEW MESSAGE ARRIVED ");  
		}else if(logLevel.toLowerCase().equals("debug")){
			 //Console Log
	    	System.out.println("--"+ clientID + "-- TOPIC: " + topic + " NEW MESSAGE ARRIVED: "+ message);
	    	//File Log
	    	log.info("--"+ clientID + "-- TOPIC: " + topic + " NEW MESSAGE ARRIVED: "+ message);  
		}   		
    
         		
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    	
    	if(logLevel.toLowerCase().equals("info")){
		 System.out.println("-- "+ clientID + "-- DELIVERY COMPLETE " );
	    	//File Log
	     log.info("-- "+ clientID + "-- DELIVERY COMPLETE " );
		}else if(logLevel.toLowerCase().equals("debug")){
			 System.out.println("-- "+ clientID + "-- DELIVERY COMPLETE :" + token.toString());
		    	//File Log
		     log.info("-- "+ clientID + "-- DELIVERY COMPLETE: " + token.toString() );
		} 
       
    }

	@Override
	public boolean awaitMessage(int duration, TimeUnit unit, String waitResponsePatter) throws InterruptedException {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isConnected() {
		return this.client.isConnected();
	}

	@Override
	public void sendMessage(String message) throws IOException {
		// TODO Auto-generated method stub
		
	}
}
