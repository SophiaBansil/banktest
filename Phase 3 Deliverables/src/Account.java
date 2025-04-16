import java.util.ArrayList;
import java.util.List;

public class Account {
	private static int count = 1;
	private float balance;
	private String id;
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
	public String getTransHistory() {
		return "";
	}
	private void setID() {
        this.id = "ACC" + count++;
    }
	public void active() {isOpen = true;}
	public void offline() {isOpen = false;}
}
