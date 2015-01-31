package org.datagaiden.moor.domain;



public class ChatMessage {

	private String message;
	private String type;
	private String userId;

	public ChatMessage(String message,String type)
	{
		this.message = message;
		this.type = type;
	}
	
	public ChatMessage(String message,String type, String userId)
	{
		this.message = message;
		this.type = type;
		this.userId = userId;
	}
	
	public ChatMessage()
	{
		
	}


	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
