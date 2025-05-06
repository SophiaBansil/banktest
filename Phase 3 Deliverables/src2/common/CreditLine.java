package src2.common;
import java.math.BigDecimal;
import java.util.List;


/* FROM DESIGN DOC:
- Tellers can only create a line of credit for customers who already 
have a checking account with at least $1000 in balance
- Clients will have a third of corresponding checking account balance at the time of creation
- Tellers will set the credit limit when creating a new credit line for the client
*/ 
public class CreditLine extends Account {
	private BigDecimal creditLimit;  // maximum overdraft (positive amount)

    public CreditLine(String limit) {
        super();
        this.creditLimit = new BigDecimal(limit);
    }

    //constructor for client-side refresh
    // constructor for client-side refresh
	public CreditLine(String id, BigDecimal balance,  List<Transaction> history, BigDecimal limit) {
		super(id, balance, history);
        this.creditLimit = limit;
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
