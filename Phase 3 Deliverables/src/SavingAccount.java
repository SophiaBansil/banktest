
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
	private int getWithdrawLimit() {
		return withdrawLimit;
	}
}
