import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SavingAccount extends Account {
	private int withdrawCount; // counts current withdraws made to account
	private int withdrawLimit; // maximum number of withdraws that can be made
	
	// default if no limit is specified
    private static final int DEFAULT_MONTHLY_WITHDRAWAL_LIMIT = 5;
    // tracks when we last zeroed out withdrawCount
    private LocalDate lastReset;
    
    public SavingAccount() {
    	// uses the other constructor, but with the default limit (wow)
    	this(DEFAULT_MONTHLY_WITHDRAWAL_LIMIT);
	}
	
	public SavingAccount(int limit) {
		super();
		withdrawLimit = limit;
		this.lastReset = LocalDate.now();
        this.withdrawCount = 0;
	}

    // constructor for client-side account refresh
    public SavingAccount(String id, BigDecimal balance, List<Transaction> history, int withdrawCount, int withdrawLimit) {
        super(id, balance, history); 
        this.withdrawLimit = withdrawLimit;
        this.withdrawCount = withdrawCount;
        this.lastReset = null; //  client never has to access this field
    }
	
	public void setWithdrawLimit(int limit) {
		this.withdrawLimit = limit;
	}
	public int getWithdrawLimit() {
		return withdrawLimit;
	}
	public void setWithdrawCount(int withdrawCount2) {
		this.withdrawCount = withdrawCount2;	
	}
	public int getWithdrawCount() {
		// ensure count is up-to-date before showing
        checkReset();
		return withdrawCount;
	}
    public LocalDate getReset(){
        return this.lastReset;
    }
	/**-- Internal reset logic --**/
    private void checkReset() {
        LocalDate now = LocalDate.now();
        if (now.getYear() != lastReset.getYear() ||
            now.getMonth() != lastReset.getMonth()) {
            withdrawCount = 0;
            lastReset = now;
        }
    }

    /**-- Main transaction hook --**/
    @Override
    public void addTransaction(Transaction trans) {
        // reset if we've crossed into a new month
        checkReset();

        if (trans.getOperation() == Transaction.OPERATION.WITHDRAW) {
            if (withdrawCount >= withdrawLimit) {
                throw new IllegalStateException("Withdrawal limit reached");
            }
            withdrawCount++;
        }

        super.addTransaction(trans);
    }
}
