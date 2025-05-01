import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public abstract class Account {
	private static int count = 1;
	private String id;
	private BigDecimal balance;
	private List<Transaction> transactionHistory;
	
	protected Account() {
		this.balance = new BigDecimal(0);
        this.transactionHistory = new ArrayList<>();
        setID();
    }
	public BigDecimal getBalance() {
		return this.balance;
	}
	public String getID() {
		return this.id;
	}
	public List<Transaction> getTransactionHistory() {
		return this.transactionHistory;
	}
	public void addTransaction(Transaction trans) {
		this.transactionHistory.add(trans);
		this.balance.add(trans.getAmount());
	}
	private void setID() {
        this.id = "ACC" + count++;
    }
}
