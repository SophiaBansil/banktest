import java.time.LocalDate;

public class SavingAccount extends Account {
	// Should reset withdrawCount every Month
	private int withdrawCount; // counts current withdraws made to account
	private static final int DEFAULT_MONTHLY_WITHDRAWAL_LIMIT = 5;
    private LocalDate reset;

	
	public SavingAccount() {
		super();
	}
	
	public int getWithdrawCount() {
		return withdrawCount;
	}

	// resets every new calendar month
	private void checkReset() {
        LocalDate now = LocalDate.now();
        if (now.getMonth() != reset.getMonth() || now.getYear() != reset.getYear()) {
            withdrawCount = 0;
            reset = now;
        }
    }

	@Override
    public void addTransaction(Transaction trans) {
		checkReset();
		
        if (trans.getOperation() == Transaction.OPERATION.WITHDRAW) {
            if (withdrawCount >= DEFAULT_MONTHLY_WITHDRAWAL_LIMIT) {
                throw new IllegalStateException("Withdrawal limit reached");
            }
            withdrawCount++;
        }

        super.addTransaction(trans);
    }
}
