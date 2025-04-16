import java.util.Date;

public class Transaction {
	private Date created;
	private float amount;
	private String note;
	
	public Transaction(float amount, String note) {
		this.created = new Date();
	}
	public String getDate() {
		return created.toString();
	}
	public float getAmount() {
		return amount;
	}
	public String getNote() {
		return note;
	}
}
