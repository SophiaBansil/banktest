package com.bankapp.common;
import com.bankapp.common.Message.TYPE;

public class FailureMessage extends Message {
    private static final long serialVersionUID = 1L;
    private final String message;

    public FailureMessage(String message) {
        super(TYPE.FAILURE, null); // You can define this enum in the Message class
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
