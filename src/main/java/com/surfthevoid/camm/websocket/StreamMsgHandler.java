package com.surfthevoid.camm.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.surfthevoid.camm.video.VideoSource;

@Component
public class StreamMsgHandler extends TextWebSocketHandler {
	
	private final Log log = LogFactory.getLog(StreamMsgHandler.class);

	protected VideoSource videoSource;
	private byte[] currentBinary;
	private Map<String, WebSocketSession> sessions = new HashMap<>();
	private Map<String, WebSocketSession> sessionsToUnregister = new HashMap<>();

	public StreamMsgHandler(VideoSource videoSource) throws JsonProcessingException {
		this.videoSource = videoSource;
	}

	@Override
	protected void handleTextMessage(final WebSocketSession session, TextMessage message) {
		String value = message.getPayload();
		try {
			if (value.equalsIgnoreCase("data")) {
				register(session);
			} else if (value.equalsIgnoreCase("close")) {
				closeAndMarkAsToUnregister(session);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		if (closeStatus != CloseStatus.NORMAL) {
			closeAndMarkAsToUnregister(session);
		}
	}
	
	@Scheduled(fixedRate=50)
	private void rawStream() {
		synchronized (sessions) {
			unregisterOldSessions();
			try {
				if (sessions.isEmpty()) {
					log.info("Waiting for new client to register");
					videoSource.close(true);
					sessions.wait();
				}
				currentBinary = videoSource.getJPEGStreamBytes();
				// WARN: do not use parallel stream, otherwise the messages
				// will be not send in the synchronized transaction
				sessions.values().stream().filter(WebSocketSession::isOpen)
						.forEach(StreamMsgHandler.this::sendBinary);
			} catch (InterruptedException e) {
				log.error("could not wait for new sessions to register", e);
			}
		}
	}

	private void register(WebSocketSession session) {
		synchronized (sessions) {
			if (session != null && session.isOpen() && !sessions.containsKey(session.getId())) {
				sessions.put(session.getId(), session);
				log.info("Session " + session.getId() + " just registered");
				sessions.notify();
			}
		}
	}

	private void sendBinary(WebSocketSession session) {
		try {
			session.sendMessage(new BinaryMessage(currentBinary));
		} catch (IOException e) {
			log.error("could not sent binary", e);
		}
	}

	private void closeAndMarkAsToUnregister(WebSocketSession session) throws IOException {
		if (session != null) {
			if (session.isOpen()) {
				try {
					session.close(CloseStatus.NORMAL);
					log.info("Session " + session.getId() + " was close correctly");
				} catch (IOException e) {
					log.error("Session " + session.getId() + " could not be closed", e);
				}
			}
			synchronized (sessionsToUnregister) {
				sessionsToUnregister.put(session.getId(), session);
				log.info("Session " + session.getId() + " is mark as to be unregistered");
			}
		}
	}
	
	private void unregisterOldSessions(){
		synchronized (sessionsToUnregister) {
			Iterator<WebSocketSession> it = sessionsToUnregister.values().iterator();
			while(it.hasNext()){
				WebSocketSession session = it.next();
				closeAndUnregister(session);
				it.remove();
			}
		}
	}
	
	private void closeAndUnregister(WebSocketSession session){
		if (session != null) {
			synchronized (sessions) {
				sessions.remove(session.getId());
				log.info("Session " + session.getId() + " was unregistered");
			}
		}
	}
}
