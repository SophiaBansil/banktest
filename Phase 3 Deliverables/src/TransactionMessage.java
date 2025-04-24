import java.util.Date;

public final class TransactionMessage extends Message {
	private static final long serialVersionUID = 1L;
	public enum OPERATION {
		WITHDRAW,
		DEPOSIT
	}
	private Date created;
	private String amount;
	private OPERATION op;
	
	public TransactionMessage(SessionInfo session_id, String amount) {
		super(Message.TYPE.TRANSACTION, session_id);
		this.amount = amount;
		this.created = new Date();
	}
	public Date getDate() {
		return created;
	}
	public String getAmount() {
		return amount;
	}
	public OPERATION getOperation() {
		return op;
	}
}
