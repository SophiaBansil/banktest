import java.text.SimpleDateFormat;
import java.util.Date;

public class Transaction {
	private Date created;
	private float amount;
	private String note;
	
	public Transaction(float amount, String note) {
		this.created = new Date();
	}
	public String getDate() {
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		String formatDate = format.format(created);
		return formatDate;
	}
	public float getAmount() {
		return amount;
	}
	public String getNote() {
		return note;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CLIENT|");
		sb.append(getDate()).append("|");
		sb.append(getAmount()).append("|");
		if (note != null) {
			sb.append(note);
		}	
		
		sb.append("~");
		sb.append(System.lineSeparator());
		
		return sb.toString();
	}
}
