package com.bankapp.common;
import java.math.BigDecimal;

public final class AccountSummary {
	public enum ACCOUNT_TYPE {
		CHECKING,
		SAVING,
		CREDIT_LINE
	}
	private final String accountID;
    private final ACCOUNT_TYPE type;
    private final String balance;  // big-decimal→string

    public AccountSummary(String id, ACCOUNT_TYPE type, BigDecimal bal) {
        this.accountID = id;
        this.type      = type;
        this.balance   = bal.toPlainString();
    }
    
    public String getID() {
    	return this.accountID;
    }
    
    public ACCOUNT_TYPE getType() {
    	return this.type;
    }
    
    public String getBalance() {
    	return this.balance;
    }
}
