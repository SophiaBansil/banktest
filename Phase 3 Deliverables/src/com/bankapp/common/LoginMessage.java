package com.bankapp.common;
import com.bankapp.common.Message.TYPE;


public final class LoginMessage extends Message {
	private static final long serialVersionUID = 1L;
	
	private final String username;
    private final String password;

    public LoginMessage(TYPE type, String username, String password) {
        super(type, null); // no session yet for login
        this.username = username;
        this.password = password;
    }
    public String getUsername() { 
    	return username; 
    }
    public String getPassword() { 
    	return password; 
    }
}
