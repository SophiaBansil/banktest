import java.util.List;

public final class AccountMessage extends Message {
	private static final long serialVersionUID = 1L;
	public enum ACCOUNT_TYPE {
		CHECKING,
		SAVING,
		CREDIT_LINE
	}
	private final ACCOUNT_TYPE account_type;
    private final String id;
    private final float balance;
    private final List<Transaction> transactionHistory;

    // Type-specific (optional) fields
    private final int withdrawCount; // Only for savings
    private final int withdrawalLimit; // Only for savings
    private final float creditLimit;    // Only for line of credit

    // Constructor for Checking
    public AccountMessage(TYPE type, SessionInfo session, String id, float balance, List<Transaction> transactionHistory) {
        super(type, session);
        this.account_type = ACCOUNT_TYPE.CHECKING;
        this.id = id;
        this.balance = balance;
        this.transactionHistory = transactionHistory;
		this.withdrawCount = 0;
		this.withdrawalLimit = 0;
		this.creditLimit = 0;
    }

    // Constructor for Savings
    public AccountMessage(TYPE type, SessionInfo session, String id, float balance, List<Transaction> transactionHistory,
                          int withdrawCount, int withdrawalLimit) {
        super(type, session);
        this.account_type = ACCOUNT_TYPE.SAVING;
        this.id = id;
        this.balance = balance;
        this.transactionHistory = transactionHistory;
        this.withdrawCount = withdrawCount;
        this.withdrawalLimit = withdrawalLimit;
		this.creditLimit = 0;
    }

    // Constructor for Line of Credit
    public AccountMessage(TYPE type, SessionInfo session, String id, float balance, List<Transaction> transactionHistory,
                          float creditLimit) {
        super(type, session);
        this.account_type = ACCOUNT_TYPE.CREDIT_LINE;
        this.id = id;
        this.balance = balance;
        this.transactionHistory = transactionHistory;
		this.withdrawCount = 0;
		this.withdrawalLimit = 0;
        this.creditLimit = creditLimit;
    }

	public ACCOUNT_TYPE getAccount_type() {
		return account_type;
	}

	public String getId() {
		return id;
	}

	public float getBalance() {
		return balance;
	}

	public List<Transaction> getTransactionHistory() {
		return transactionHistory;
	}

	public int getWithdrawCount() {
		return withdrawCount;
	}

	public int getWithdrawalLimit() {
		return withdrawalLimit;
	}

	public Float getCreditLimit() {
		return creditLimit;
	}
}
