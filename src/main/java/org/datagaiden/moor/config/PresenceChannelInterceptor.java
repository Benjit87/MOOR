package org.datagaiden.moor.config;

import org.apache.log4j.Logger;
import org.datagaiden.moor.domain.ChatMessage;
import org.datagaiden.moor.domain.UserList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;

public class PresenceChannelInterceptor extends ChannelInterceptorAdapter {
	 
    static Logger log = Logger.getLogger(PresenceChannelInterceptor.class.getName());
    @Autowired
    private UserList userList;
    @Lazy
	@Autowired
    private SimpMessagingTemplate brokerMessagingTemplate;
	
    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
 
    	
    	StompHeaderAccessor sha = StompHeaderAccessor.wrap(message);

        if(sha.getCommand() == null) {
            return;
        }
        String sessionId = sha.getSessionId();
        String userId;
        switch(sha.getCommand()) {
            case CONNECT:
            	userId = sha.getNativeHeader("userId").get(0);
            	userList.addUser(sessionId, userId);
                log.debug("STOMP Connect [sessionId: " + sessionId + ", userId: " + userId + "]");
                break;
            case CONNECTED:
                log.debug("STOMP Connected [sessionId: " + sessionId + "]");
                break;
            case SUBSCRIBE:
            	String channelsub = sha.getNativeHeader("destination").get(0);
            	log.debug("STOMP Subscribe [sessionId: " + sessionId + ", subscription: " + channelsub + "]");
            	if (channelsub.equals("/topic/userlist")) { 
            		brokerMessagingTemplate.convertAndSend("/topic/userlist",userList.getUserList());
            	};
            	break;
            case DISCONNECT:
            	userId = userList.removeUser(sessionId);
            	brokerMessagingTemplate.convertAndSend("/topic/userlist",userList.getUserList());
                log.debug("STOMP Disconnect [sessionId: " + sessionId + ", userId: " + userId + "]");             
                break;
            case SEND:
            	log.debug("STOMP Send [sessionId: " + sessionId + "]");
            	break;
            default:
                break;
 
        }
    }
    
}