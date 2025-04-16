import java.util.ArrayList;
import java.util.List;

public class Account {
	private static int count = 1;
	private String id;
	private float balance;
	private boolean isOpen;
	private List<Transaction> transactionHistory;
	
	public Account() {
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
	public void active() {this.isOpen = true;}
	public void offline() {this.isOpen = false;}
	private void setID() {
        this.id = "ACC" + count++;
    }
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("ACCOUNT|");
		sb.append(getID()).append("|");
		sb.append(getBalance()).append("|");
		
		sb.append("~");
		sb.append(System.lineSeparator());
		
		for (Transaction t : transactionHistory) {
			sb.append(t.toString());
		}
		return sb.toString();
	}
}
