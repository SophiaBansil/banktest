import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Transaction {
	public enum OPERATION {
		WITHDRAW,
		DEPOSIT
	}
	private Date created;
	private BigDecimal amount;
	private OPERATION op;
	
	public Transaction(double amount, OPERATION op) {
		this.created = new Date();
		this.amount = BigDecimal.valueOf(amount);
	}
	public String getDate() {
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		String formatDate = format.format(created);
		return formatDate;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public OPERATION getOperation() {
		return op;
	}
}
