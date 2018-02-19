package blazemeter.jmeter.plugins.websocket.sampler;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public abstract class Handler implements Serializable{
	
	private String encoding;
	protected Queue<String> messages = new LinkedList<String>();
	 
	Handler (){}
	
	Handler (String enconding){
		this.encoding = encoding;
	}
	
	public String getContentEncoding(){
		return this.encoding;
	}
	
	public abstract void close();
	
	public abstract boolean awaitMessage(int duration, TimeUnit unit, String waitResponsePatter) throws InterruptedException;
	
	 public String getMessages(){
    	String ret = "";
    	for (String s : messages){
    		ret = ret + s + "\n";
    	}
    	messages.clear();
    	return ret;
    }
	
	public abstract void initialize();
	
	public abstract boolean isConnected();
	
	public abstract void sendMessage(String message) throws IOException;

}
