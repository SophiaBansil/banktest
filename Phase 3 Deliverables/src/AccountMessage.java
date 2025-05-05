import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class AccountMessage extends Message {
	private static final long serialVersionUID = 1L;

	public enum ACCOUNT_TYPE {
		CHECKING,
		SAVING,
		CREDIT_LINE
	}

	private final ACCOUNT_TYPE account_type;
	private final String username_owner;
	private final String id;
	private final String balance; // Now a String
	private final List<Transaction> transactionHistory;

	// Type-specific 
	private final int withdrawCount; // Only for savings
	private final int withdrawLimit; // Only for savings
	private LocalDate reset;		// only for savings
	private final String creditLimit; // Only for creditLimit
	

	// Constructor for Requests
	public AccountMessage(TYPE type, SessionInfo session, String username, String account_id) {
		super(type, session);
		this.account_type = null;
		this.id = account_id;
		this.username_owner = username;
		this.balance = "0";
		this.transactionHistory = null;
		this.withdrawCount = 0;
		this.withdrawLimit = 0;
		this.creditLimit = "0";
	}

	// Constructor for CREATE_NEW_ACCOUNT
	public AccountMessage(SessionInfo session, String username, ACCOUNT_TYPE type, String creditLim, int wLimit) {
        super(TYPE.CREATE_ACCOUNT, session); 
		
		this.username_owner = username;
        this.account_type = type;
        this.id = null; 
        this.balance = "0";
        this.transactionHistory = null;
        this.withdrawCount = 0;
		
		if (type == ACCOUNT_TYPE.CREDIT_LINE) {
            
            this.creditLimit = creditLim;
        } else {
             this.creditLimit = "0";
		}

		if (type == ACCOUNT_TYPE.SAVING){
			this.withdrawLimit = wLimit;
		} else {
			this.withdrawLimit = 0;
		}
    }

	// Constructor for Checking
	public AccountMessage(TYPE type, 
			SessionInfo session, 
			String username, 
			String id, 
			BigDecimal balance, 
			List<Transaction> transactionHistory) 
	{
	    super(type, session);
	    this.account_type = ACCOUNT_TYPE.CHECKING;
	    this.id = id;
	    this.username_owner = username; // ✅ added
	    this.balance = balance.toPlainString();
	    this.transactionHistory = transactionHistory;
	    this.withdrawCount = 0;
	    this.withdrawLimit = 0;
	    this.creditLimit = "0";
	}

	// Constructor for Savings
	public AccountMessage(TYPE type, 
			SessionInfo session, 
			String username, 
			String id, 
			BigDecimal balance, 
			List<Transaction> transactionHistory,
            int withdrawCount,
            int withdrawLimit,
			LocalDate lastReset) 
	{
		super(type, session);
		this.account_type = ACCOUNT_TYPE.SAVING;
		this.id = id;
		this.username_owner = username;
		this.balance = balance.toPlainString();
		this.transactionHistory = transactionHistory;
		this.withdrawCount = withdrawCount;
		this.withdrawLimit = withdrawLimit;
		this.creditLimit = "0";
		this.reset = lastReset;
	}

	// Constructor for Line of Credit
	public AccountMessage(TYPE type, 
			SessionInfo session, 
			String username, 
			String id, 
			BigDecimal balance, 
			List<Transaction> transactionHistory,
            BigDecimal creditLimit) 
	{
		super(type, session);
		this.account_type = ACCOUNT_TYPE.CREDIT_LINE;
		this.id = id;
		this.username_owner = username;
		this.balance = balance.toPlainString();
		this.transactionHistory = transactionHistory;
		this.withdrawCount = 0;
		this.withdrawLimit = 0;
		this.creditLimit = creditLimit.toPlainString();
	}

	// Getters
	public ACCOUNT_TYPE getAccountType() {
		return account_type;
	}

	public String getID() {
		return id;
	}

	public BigDecimal getBalance() {
		return new BigDecimal(balance);
	}

	public List<Transaction> getTransactionHistory() {
		return transactionHistory;
	}
	
	public int getWithdrawLimit() {
		return withdrawLimit;
	}

	public int getWithdrawCount() {
		return withdrawCount;
	}
	
	public String getUsername() {
		return username_owner;
	}

	public BigDecimal getCreditLimit() {
		return new BigDecimal(creditLimit);
	}
}
