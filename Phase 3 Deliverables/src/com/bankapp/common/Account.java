package com.bankapp.common;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public abstract class Account implements Serializable{
	private String id;
	private BigDecimal balance;
	private List<Transaction> transactionHistory;
	
	protected Account() {
		this.balance = new BigDecimal(0);
        this.transactionHistory = new ArrayList<>();
    }

	//constructor for client-side account refresh;
	protected Account(String id, BigDecimal b, List<Transaction> history) {
		this.id = id;
		this.balance = b;
		this.transactionHistory = history;
	 }
	public BigDecimal getBalance() {
		return this.balance;
	}
	public String getID() {
		return this.id;
	}
	public List<Transaction> getTransactionHistory() {
		return this.transactionHistory;
	}
	public void addTransaction(Transaction trans) {
		this.transactionHistory.add(trans);
		this.balance = this.balance.add(trans.getAmount());
	}
	public void setID(String id) {
        if (this.id == null && id != null && !id.trim().isEmpty()) {
            this.id = id.trim();
        } else if (this.id != null) {
            System.err.println("cannot overwrite ID");
        } 
    }
}
