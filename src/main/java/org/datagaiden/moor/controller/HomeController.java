package org.datagaiden.moor.controller;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.datagaiden.moor.domain.Admin;
import org.datagaiden.moor.domain.ChatMessage;
import org.datagaiden.moor.domain.UserList;
import org.datagaiden.moor.mq.MqClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class HomeController {
	
	static Logger log = Logger.getLogger(HomeController.class.getName());
	
	@Autowired
	private MqClient mqClient;
	@Autowired
	private UserList userList;
	@Autowired
    private SimpMessagingTemplate brokerMessagingTemplate;
	@Autowired
	ServletContext servletContext;
	
	@RequestMapping("/")
    String home() {
        return "index";
    }
	
	@RequestMapping(value = "/admin", method = RequestMethod.GET)
    String admin(Model model) {
		model.addAttribute("zeromq",mqClient.getIp());
        return "admin";
    }
	
	@RequestMapping(value = "/admin", method = RequestMethod.POST)
    String adminSubmit(@ModelAttribute("form") Admin form, Model model) {
		log.info("Updating ZeroMq Settings: " + form.getZeromq());
		mqClient.reconnect(form.getZeromq());
		model.addAttribute("zeromq",mqClient.getIp());
        return "admin";
    }
	
	
	@RequestMapping(value = "/login", method = RequestMethod.GET)
    String login() {
        return "login";
    }
	
	@RequestMapping(value = "/login", method = RequestMethod.POST)
    String loginRedirect() {
        return "redirect:/admin";
    }
	

    @MessageMapping("/moor")
    @SendTo("/topic/mainroom")
    public ChatMessage greeting(ChatMessage chatmessage, SimpMessageHeaderAccessor headerAccessor) throws Exception {
    	log.debug("Received message: " + chatmessage.getMessage() + ", type: " + chatmessage.getType() + ", sessionId : " + headerAccessor.getSessionId());
        chatmessage.setUserId(userList.getUser(headerAccessor.getSessionId()));
    	return chatmessage;
    }
    
	@RequestMapping(value = "/sendmessage", method = RequestMethod.POST, consumes = "application/json" )
	@ResponseBody
	public boolean rMessage(@RequestBody final ChatMessage chatmessage) {
		log.debug("Received message: " + chatmessage.getMessage() + ", type: " + chatmessage.getType());
		String message = mqClient.sendMessages(chatmessage.getMessage());
		if (message.isEmpty()) {
			brokerMessagingTemplate.convertAndSend("/topic/mainroom",new ChatMessage(chatmessage.getMessage(),"R",chatmessage.getUserId()));
			log.debug("Broadcasting message: " + message + ", type: " + "R Empty");
			return true; } // A R command that have no response
		else if (message.equals("plotgenerated"))
		{
			brokerMessagingTemplate.convertAndSend("/topic/mainroom",new ChatMessage(mqClient.getImageCounter(),"Rplot",chatmessage.getUserId()));
			log.debug("Broadcasting message: " + mqClient.getImageCounter() + ", type: " + "Rplot");	
		}
		else 
		{
			brokerMessagingTemplate.convertAndSend("/topic/mainroom",new ChatMessage(message,"R",chatmessage.getUserId()));
			log.debug("Broadcasting message: " + message + ", type: " + "R");
		}
		return true; // Send back via POST to acknowledge request
	}
	
	@RequestMapping(value = "/plots/{plotnum}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
	@ResponseBody
	public byte[] getPlot(@PathVariable("plotnum") String imageCounter, HttpServletRequest request) throws IOException {
		
	    
	    return FileCopyUtils.copyToByteArray(new File("plot"+imageCounter+".png"));
	}
	
	@RequestMapping(value = "/variables", method = RequestMethod.GET, produces = "application/json" )
	@ResponseBody
	public List<String> getVariables()
	{
		String message = mqClient.sendMessages("cat(ls())");
		System.out.println(message);
		List<String> variableList = Arrays.asList(message.split(" "));
		return variableList;
	}
 
}
