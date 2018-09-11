package com.oxygenxml.cmis.core;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "userCredentials")
public class UserCredentials {
	
	private String username;
	private String password;

	public UserCredentials() {
	}

	public UserCredentials(String username, String password) {		
		this.username = username;	
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {		
		this.password = password;
	}

	public boolean isEmpty() {
		if(username.isEmpty() && password.isEmpty()) {
			return true;
		}
		if(!username.isEmpty() && password.isEmpty()) {
			return false;
		}
		return false;
	}

	@Override
	public String toString() {
		return "Username=" + this.username;
	}

}
