import java.math.BigDecimal;


/* FROM DESIGN DOC:
- Tellers can only create a line of credit for customers who already 
have a checking account with at least $1000 in balance
- Clients will have a third of corresponding checking account balance at the time of creation
- Tellers will set the credit limit when creating a new credit line for the client
*/ 
public class CreditLine extends Account {
	private BigDecimal creditLimit;  // maximum overdraft (positive amount)

    public CreditLine(String limit) {
        this.creditLimit = new BigDecimal(limit);
    }
    
    public void setCreditLimit(String newLimit) {
        this.creditLimit = new BigDecimal(newLimit);
    }
    
    public BigDecimal getCreditLimit() {
        return creditLimit;
    }


    @Override
    public void addTransaction(Transaction trans) {
        // trans.getAmount() is negative for withdrawals,
        // positive for deposits:
        BigDecimal newBalance = getBalance().add(trans.getAmount());

        // check if going over limit
        if (newBalance.compareTo(creditLimit.negate()) < 0) {
            throw new IllegalStateException("Credit limit exceeded: would go to " 
                + newBalance.toPlainString());
        }

        super.addTransaction(trans);
    }
}
