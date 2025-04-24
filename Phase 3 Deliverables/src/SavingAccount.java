
public class SavingAccount extends Account {
	// Should reset withdrawCount every Month
	private int withdrawCount; // counts current withdraws made to account
	private int withdrawLimit; // maximum number of withdraws that can be made
	
	public SavingAccount(int limit) {
		super();
		withdrawLimit = limit;
	}
	
	public void setWithdrawLimit(int limit) {
		this.withdrawLimit = limit;
	}
	public int getWithdrawLimit() {
		return withdrawLimit;
	}
	public int getWithdrawCount() {
		return withdrawCount;
	}
	@Override
    public void addTransaction(Transaction trans) {
        // 1) If it's a withdrawal, enforce your rules:
        if (trans.getOperation() == Transaction.OPERATION.WITHDRAW) {
            if (withdrawCount >= withdrawLimit) {
                throw new IllegalStateException("Withdrawal limit reached");
            }
            withdrawCount++;
        }

        // 2) Do the common work:
        super.addTransaction(trans);
    }
}
