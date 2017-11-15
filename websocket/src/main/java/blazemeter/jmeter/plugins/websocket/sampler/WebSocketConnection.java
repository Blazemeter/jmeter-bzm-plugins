package blazemeter.jmeter.plugins.websocket.sampler;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

@WebSocket(maxTextMessageSize = 256 * 1024 * 1024)
public class WebSocketConnection extends Handler implements Serializable{

	private static final Logger log = LoggingManager.getLoggerForClass();
	
	private WebSocketClient webSocketClient;
	private Session session;
	private boolean connected;
	
	protected CountDownLatch openLatch = new CountDownLatch(1);
    protected CountDownLatch closeLatch = new CountDownLatch(1);
    protected CountDownLatch messageLatch = new CountDownLatch(1);
	
	private String closeConnectionPattern;
	private Pattern closeConnectionExpression;
	
	private String waitResponsePatter;
	private Pattern waitResponseExpresion;
	
	private boolean waitMessage;
	
	public WebSocketConnection(WebSocketClient webSocketClient, String closeConnectionPattern){
		new WebSocketConnection(webSocketClient, closeConnectionPattern, "utf-8");
	}
	
	public WebSocketConnection(WebSocketClient webSocketClient, String closeConnectionPattern, String encoding) {
		super(encoding);
		this.webSocketClient = webSocketClient;
		this.closeConnectionPattern= new CompoundVariable(closeConnectionPattern).execute();
		initializePatterns();
		this.waitMessage = false;
	}
	
	@OnWebSocketMessage
    public void onMessage(String msg) {
        messages.add("[RECIVED at "+ System.currentTimeMillis() + "] " + msg);
        if (!closeConnectionPattern.isEmpty()){
        	if (closeConnectionExpression.matcher(msg).find()) {
                closeLatch.countDown();
                close();
            }
        }   
        if (this.waitMessage){
        	if (!waitResponsePatter.isEmpty()){
            	if (waitResponseExpresion.matcher(msg).find()) {
                    messageLatch.countDown();
                    this.waitMessage = false;
                }
            }   
        }
    }

    @OnWebSocketConnect
    public void onConect(Session session) {
        this.session = session;
        connected = true;
        openLatch.countDown();
    }

    @OnWebSocketError
    public void onError(Throwable cause){
    	log.error("Error " + cause.getMessage());
    }
    
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
    	if (statusCode != StatusCode.NORMAL)
    		log.error("Disconnect " + statusCode + ": " + reason);
    	else
    		log.debug("Disconnect " + statusCode + ": " + reason);
        openLatch.countDown();
        closeLatch.countDown();
        connected = false;
    }
    
    public void sendMessage(String message) throws IOException {
        session.getRemote().sendString(message);
        messages.add("[SEND at "+ System.currentTimeMillis() + "] " + message);
    }
	
    public boolean isConnected() {
        return connected;
    }
    
    public void initialize() {
        this.closeLatch = new CountDownLatch(1);
    }
	
	protected void initializePatterns() {
        try {
        	closeConnectionExpression = (closeConnectionPattern != null || !closeConnectionPattern.isEmpty()) ? Pattern.compile(closeConnectionPattern) : null;
        } catch (Exception ex) {
            log.error("Error close connection patern: " + ex.getMessage());
        }
    }
	
	public void close() {
        close(StatusCode.NORMAL, "JMeter closed session.");
    }

    public void close(int statusCode, String statusText) {
        if (session != null) {
            session.close(statusCode, statusText);
            this.connected = false;
        } else {
        	log.error("Error closing connection, session wasn't started");
        }

        try {
        	webSocketClient.stop();
        } catch (Exception e) {
        	log.error("Error closing connection: " + e.getMessage());
        }
    }
	

	public boolean awaitOpen(int duration, TimeUnit unit) throws InterruptedException {
        boolean res = this.openLatch.await(duration, unit);
        return res;
    }
	
	//TODO Fix scape characters
	 public boolean awaitMessage(int duration, TimeUnit unit, String waitResponsePatter) throws InterruptedException {
	 	this.waitResponsePatter = new CompoundVariable(waitResponsePatter).execute();
        this.waitResponseExpresion = (this.waitResponsePatter != null || !this.waitResponsePatter.isEmpty()) ? Pattern.compile(this.waitResponsePatter) : null;
        this.waitMessage = true;
        boolean res = this.messageLatch.await(duration, unit);
        //if the message didnt came in the time specified it could came before
        if (!res){
        	for (String m : this.messages){
        		if (waitResponseExpresion.matcher(m).find())
        			return true;
        	}
        }
        return res;
    }
}
