package com.bankapp.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.bankapp.common.Account;
import com.bankapp.common.AccountMessage;
import com.bankapp.common.CheckingAccount;
import com.bankapp.common.ClientProfile;
import com.bankapp.common.CreditLine;
import com.bankapp.common.FailureMessage;
import com.bankapp.common.Message;
import com.bankapp.common.SavingAccount;
import com.bankapp.common.SessionInfo;
import com.bankapp.common.SuccessMessage;
import com.bankapp.common.Transaction;
import com.bankapp.common.TransactionMessage;
import com.bankapp.common.Message.TYPE;
import com.bankapp.common.Transaction.OPERATION;

public class ATMApplication {
    private ConnectionHandler handler;
    private SessionInfo session;
    private Account account;
    private ClientProfile client;
    private static final BigDecimal ATM_TRANSACTION_LIMIT = new BigDecimal("9999.99");

    public ATMApplication(){
  
    }
    public void setConnectionHandler(ConnectionHandler c){
        this.handler = c;
    }

    public void setSession(SessionInfo s){
        this.session = s;
    }

    public void setClient(ClientProfile c){
        this.client = c;
    }
    
    public Account getAccount(){
        return this.account;
    }

    // getter for connectionhandlerand session not needed; already in clientprofileapp

    public Message withdraw(String amount) {
        // check that inputted amount is valid
        // cannot enter negative amount
        // cannot withdraw more than $9999 at atm
        try {
			BigDecimal check = new BigDecimal(amount);
            if (check.compareTo(BigDecimal.ZERO) <= 0) {
                return new FailureMessage("Error: entered negative number");
            }
            if (check.compareTo(ATM_TRANSACTION_LIMIT) > 0){
                // print to gui: Please see a Teller for assistance.
                return new FailureMessage("Amount too high. please see teller");
            }
		} catch (NumberFormatException e) {
			return new FailureMessage("Please enter a real number");
		}
        return performTransaction(amount, OPERATION.WITHDRAW);
    }

    public Message deposit(String amount) {
        //check that inputted amount is valid
        // cannot enter negative amount
        // cannot  deposit more than $9999 at atm
        try {
			BigDecimal check = new BigDecimal(amount);
            if (check.compareTo(BigDecimal.ZERO) <= 0) {
                return new FailureMessage("Error: entered negative number");
            }
            if (check.compareTo(ATM_TRANSACTION_LIMIT) > 0){
                // print to gui: Please see a Teller for assistance.
                return new FailureMessage("Amount too high. please see teller");
            }	
		} catch (NumberFormatException e) {
			return new FailureMessage("Please enter a real number");
		}

        return performTransaction(amount, OPERATION.DEPOSIT);
    }

    // loads in all account data once an account is chosen from ClientApplication
    public Message loadAccount (String accID) {
        Message loadAccMsg = new AccountMessage(Message.TYPE.LOAD_ACCOUNT, this.session, this.client.getUsername(), accID );
        handler.send(loadAccMsg);
    
        try {
            Message serverResponse = handler.getMessage(); 
    
            if (serverResponse instanceof AccountMessage) {
                AccountMessage msg = (AccountMessage) serverResponse;
                switch (msg.getAccountType()) {
                    case CHECKING:
                        this.account = createCheckingAccount(msg);
                        return msg;
                    case SAVING:
                        this.account = createSavingAccount(msg);
                        return msg;
                    case CREDIT_LINE:
                        this.account = createCreditAccount(msg);
                        return msg;
                    default:
                         // Should not happen if server sends valid type
                        System.err.println("Received unknown account type from server.");
                        this.account = null;
                        return new FailureMessage("Please enter a real number");
                }
       
            } else if (serverResponse instanceof FailureMessage) {
                System.err.println("Failed to load account " + accID + ": " + ((FailureMessage) serverResponse).getMessage());
                this.account = null; 
                return serverResponse;
            } else if (serverResponse == null) {
                 System.err.println("Timeout or interruption while waiting for account load response.");
                 this.account = null;
                 return serverResponse;
            }
             else {
                System.err.println("Received something unexpected " + serverResponse.getClass().getName());
                 this.account = null;
                return serverResponse;
            }
        } catch (Exception e) {
            System.err.println("Error during loadaccount: " + e.getMessage());
             this.account = null;
            return new FailureMessage("exception during loadaccount");
        }
    }

    public Message loadTransactionHistory(){
        //return TRANSACTION HIST TO GUI
        if (account == null || account.getTransactionHistory() == null) {
            //gui.showError("No transaction history available");
            return new FailureMessage("There is no history available.");
        }
       
        /*  try {
            // sort transactions by date
            List<Transaction> transactions = new ArrayList<>(account.getTransactionHistory());
            transactions.sort((t1, t2) -> t2.getCreated().compareTo(t1.getCreated()));

            // do more formatting and build new list
            List<String> formatted = new ArrayList<>();
            for (Transaction t : transactions) {
                String formattedEntry = String.format("%s, %s, %s",
                    t.getDate(),
                    formatOperation(t.getOperation()), 
                    formatAmount(t)  
                );
                formatted.add(formattedEntry);
            }

            // relay to GUI
            // gui.displayTransactionHistory(formatted);

            } catch (Exception e) {
             // gui.showError("Error formatting transactions: " + e.getMessage());
            }
        */
        AccountMessage historyMsg = new AccountMessage(
        Message.TYPE.LOAD_ACCOUNT,            
        session,
        client.getUsername(),                 
        account.getID(),
        account.getBalance(),
        account.getTransactionHistory() );
        return historyMsg;      
   
    }



    public Message exit() {
        Message msg = new AccountMessage(Message.TYPE.EXIT_ACCOUNT, this.session, this.client.getUsername(), this.account.getID());
        handler.send(msg);
        try {
            Message response = handler.getMessage();
            if (response instanceof SuccessMessage) {
                 System.out.println("Exited account " + this.account.getID() + " successfully.");
                 this.account = null; 
                 return new SuccessMessage(null);
            } else if (response instanceof FailureMessage) {
                 System.err.println("Server failed to exit account: " + ((FailureMessage)response).getMessage());
                 return response;
            } else {
                 System.err.println("Unexpected response during account exit.");
                 return response;
            }
        } catch (Exception e) {
             System.err.println("Error during exitAccount: " + e.getMessage());
             return new FailureMessage("Caught exception during exitAccount");
        }
    }

    //------------------------
    //private helper methods
    //------------------------

    private Message performTransaction(String amount, Transaction.OPERATION operation) {

    
        // make transactionMessage
        TransactionMessage transMsg = new TransactionMessage(
            session,
            amount,
            operation,
            account.getID());
        //send it to the server
        handler.send(transMsg);
    
        // BLOCK and wait for server response
        try {
            Message response = handler.getMessage();
            if (response instanceof SuccessMessage) {
                // transaction was a success, so refresh local account data
                return loadAccount(account.getID());
            } else if (response instanceof FailureMessage) {
                System.out.println("Transaction failed: " + 
                    ((FailureMessage) response).getMessage());
                return response;
            } else{
                return new FailureMessage("unexpected error in performtransaction");
            }
            
        } catch (Exception e) {
            return new FailureMessage("Transaction error: " + e.getMessage());}
        
    }
    

    
    // method to retrieve account information from Database
    private boolean refreshAccount(){

        if (account == null) return false;
        // send LOAD_ACCOUNT request
        AccountMessage requestMsg = new AccountMessage(Message.TYPE.LOAD_ACCOUNT, session, this.client.getUsername(), account.getID());
        handler.send(requestMsg);
        // BLOCK client to wait for response
        try {
            Message serverResponse = handler.getMessage();
            if (serverResponse instanceof AccountMessage) {
                AccountMessage msg = (AccountMessage) serverResponse;
    
                switch(msg.getAccountType()) {
                    case CHECKING:
                    this.account = createCheckingAccount(msg);
                    break;
                case SAVING:
                    this.account = createSavingAccount(msg);
                    break;
                case CREDIT_LINE:
                    this.account = createCreditAccount(msg);
                    break;
                default:
                    throw new IllegalStateException("Unknown account type");
                }
                return true;
            }else if (serverResponse instanceof FailureMessage) {
                System.out.println("Error: " + ((FailureMessage) serverResponse).getMessage());
            }   
        } catch (Exception e) {
            System.out.println("Account refresh interrupted");
        }
        return false;
    }
    
     
    private Account createCheckingAccount(AccountMessage msg) {
        String id = msg.getID();
        BigDecimal balance = msg.getBalance();
        List<Transaction> history = msg.getTransactionHistory();

        CheckingAccount account = new CheckingAccount(id, balance, history);
        return account;
   }
    
    private Account createSavingAccount(AccountMessage msg) {
        String id = msg.getID();
        BigDecimal balance = msg.getBalance(); 
        List<Transaction> history = msg.getTransactionHistory(); 
        int withdrawCount = msg.getWithdrawCount();
        int withdrawLimit = msg.getWithdrawLimit();

        SavingAccount account = new SavingAccount(msg.getID(), msg.getBalance(), msg.getTransactionHistory(), msg.getWithdrawCount(), msg.getWithdrawLimit());
        return account;
    }
    
    private Account createCreditAccount(AccountMessage msg) {
       CreditLine account = new CreditLine(msg.getID(), msg.getBalance(),msg.getTransactionHistory(), msg.getCreditLimit() );
       return account;
    }

    public String formatAmount(Transaction t) {
        String sign = (t.getOperation() == Transaction.OPERATION.DEPOSIT) ? "+" : "-";
        return String.format("%s$%.2f", sign, t.getAmount().abs());
    }

    private String formatOperation(Transaction.OPERATION op) {
        if (op == null) return "Unknown";
        return op.name().charAt(0) + op.name().substring(1).toLowerCase();
    }

}

