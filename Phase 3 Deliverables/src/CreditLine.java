import java.math.BigDecimal;

public class CreditLine extends Account {
	private BigDecimal creditLimit;  // maximum overdraft (positive amount)

    /** Construct from a String limit (e.g. "500.00") */
    public CreditLine(String limit) {
        this.creditLimit = new BigDecimal(limit);
    }
    
    public void setCreditLimit(String newLimit) {
        this.creditLimit = new BigDecimal(newLimit);
    }
    
    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    /**
     * Records a transaction, but first enforces that the post-transaction
     * balance never goes below –creditLimit.
     */
    @Override
    public void addTransaction(Transaction trans) {
        // Assuming trans.getAmount() is negative for withdrawals,
        // positive for deposits:
        BigDecimal newBalance = getBalance().add(trans.getAmount());

        // If newBalance < –creditLimit, the user exceeded their line
        if (newBalance.compareTo(creditLimit.negate()) < 0) {
            throw new IllegalStateException("Credit limit exceeded: would go to " 
                + newBalance.toPlainString());
        }

        // If limit not breached, the base class record it and update balance
        super.addTransaction(trans);
    }
}
