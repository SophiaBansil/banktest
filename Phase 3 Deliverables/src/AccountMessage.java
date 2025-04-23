import java.util.List;

public final class AccountMessage extends Message {
	private static final long serialVersionUID = 1L;
	public enum ACCOUNT_TYPE {
		CHECKING,
		SAVING,
		CREDIT_LINE
	}
	private ACCOUNT_TYPE account_type;
    private String id;
    private float balance;
    private List<Transaction> transactionHistory;

    // Type-specific (optional) fields
    private int withdrawCount; // Only for savings
    private int withdrawalLimit; // Only for savings
    private float creditLimit;    // Only for line of credit

    // Constructor for Checking
    public AccountMessage(TYPE type, SessionInfo session, String id, float balance, List<Transaction> transactionHistory) {
        super(type, session);
        this.account_type = ACCOUNT_TYPE.CHECKING;
        this.id = id;
        this.balance = balance;
        this.transactionHistory = transactionHistory;
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
    }

    // Constructor for Line of Credit
    public AccountMessage(TYPE type, SessionInfo session, String id, float balance, List<Transaction> transactionHistory,
                          float creditLimit) {
        super(type, session);
        this.account_type = ACCOUNT_TYPE.CREDIT_LINE;
        this.id = id;
        this.balance = balance;
        this.transactionHistory = transactionHistory;
        this.creditLimit = creditLimit;
    }

	public ACCOUNT_TYPE getAccount_type() {
		return account_type;
	}

	public void setAccount_type(ACCOUNT_TYPE account_type) {
		this.account_type = account_type;
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
