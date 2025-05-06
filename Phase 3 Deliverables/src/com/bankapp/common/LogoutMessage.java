package com.bankapp.common;
import com.bankapp.common.Message.TYPE;


public final class LogoutMessage extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LogoutMessage(TYPE type, SessionInfo session) {
		super(type, session);
	}
}
