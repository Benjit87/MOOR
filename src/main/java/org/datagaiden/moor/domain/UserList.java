package org.datagaiden.moor.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class UserList {
	
	Map<String,String> userMap = new HashMap<String,String>();
	
	public Map<String,String> getUserMap() {
		return userMap;
	}

	public void setUserMap(Map<String,String> userMap) {
		this.userMap = userMap;
	}

	public boolean addUser(String sessionId, String userId)
	{
		if (userMap.containsKey(sessionId)) return false;
		userMap.put(sessionId, userId);
		return true;
	}
	
	public String getUser(String sessionId)
	{
		return userMap.get(sessionId);
	}
	
	public String removeUser(String sessionId)
	{
		return userMap.remove(sessionId);
	}
	
	public List<String> getUserList()
	{
		List<String> list = new ArrayList<String>(userMap.values());	
		return list;
	}

}
