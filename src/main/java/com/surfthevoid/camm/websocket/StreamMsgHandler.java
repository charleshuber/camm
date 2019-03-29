package com.surfthevoid.camm.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.surfthevoid.camm.video.VideoStreamJavaCV;

@Component
public class StreamMsgHandler extends TextWebSocketHandler {

	protected VideoStreamJavaCV videoSource;
	private Timer timer = new Timer("streamTimer");
	private byte[] currentBinary;
	private Map<String, WebSocketSession> sessions = new HashMap<>();
    
	public StreamMsgHandler(VideoStreamJavaCV videoSource) throws JsonProcessingException {
		this.videoSource = videoSource;
		stream();
	}

	@Override
	protected void handleTextMessage(final WebSocketSession session, TextMessage message) {
		String value = message.getPayload();
		try {
			if (value.equalsIgnoreCase("data")) {
				register(session);
			} else if (value.equalsIgnoreCase("close")) {
				closeAndUnregister(session);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		if(closeStatus != CloseStatus.NORMAL){
			closeAndUnregister(session);
		}
	}
	
	private void stream(){
		timer.schedule(new StreamTask(), 50L);
	}
	
	private void register(WebSocketSession session){
		synchronized(sessions){
			if(session != null 
					&& session.isOpen() 
					&& !sessions.containsKey(session.getId())){
				sessions.put(session.getId(), session);
				System.out.println("Session " + session.getId() + " just registered");
			}
			sessions.notify();
		}
	}
	
	private void sendBinary(WebSocketSession session){
		try {
			session.sendMessage(new BinaryMessage(currentBinary));
		} catch (IOException e) {
			System.out.println("could not sent binary");
		}
	}
	
	private void closeAndUnregister(WebSocketSession session) throws IOException{
		if(session != null){
			synchronized (sessions) {
				sessions.remove(session.getId());
				System.out.println("Session " + session.getId() + " is leaving");
			}
			if(session.isOpen()){
				session.close(CloseStatus.NORMAL);
			}
		}
	}
		
	private class StreamTask extends TimerTask {
		@Override
		public void run() {			
			synchronized (sessions) {
				try{
					if(sessions.isEmpty()){
						System.out.println("Waiting for new client to register");
						videoSource.close();
						sessions.wait();
					}
				currentBinary = videoSource.getBytes();
        		sessions.values().parallelStream()
        			.filter(WebSocketSession::isOpen)
        			.forEach(StreamMsgHandler.this::sendBinary); 	
				} catch(InterruptedException e){
					System.out.println("could not wait for new sessions to register");
				}
				stream();
        	}
		}	
	}

}
