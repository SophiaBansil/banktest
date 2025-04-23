import java.util.ArrayList;
import java.util.List;

public abstract class Account {
	private static int count = 1;
	private String id;
	private float balance;
	private List<Transaction> transactionHistory;
	
	protected Account() {
		this.balance = 0;
        this.transactionHistory = new ArrayList<>();
        setID();
    }
	public float getBalance() {
		return this.balance;
	}
	public String getID() {
		return this.id;
	}
	public List<Transaction> getTransHistory() {
		return this.transactionHistory;
	}
	public void addTransaction(Transaction trans) {
		this.transactionHistory.add(trans);
	}
	private void setID() {
        this.id = "ACC" + count++;
    }
}
