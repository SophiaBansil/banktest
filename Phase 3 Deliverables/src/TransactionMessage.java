import java.util.Date;

public final class TransactionMessage extends Message {
	private static final long serialVersionUID = 1L;
	private final Date created;
	private final String amount;
	private final Transaction.OPERATION op;
	private final String accountID;
	
	public TransactionMessage(SessionInfo session_id, String amount, Transaction.OPERATION op, String accountID) {
		super(Message.TYPE.TRANSACTION, session_id);
		this.amount = amount;
		this.created = new Date();
		this.op = op;
		this.accountID = accountID;
	}
	public Date getDate() {
		return created;
	}
	public String getAmount() {
		return amount;
	}
	public Transaction.OPERATION getOperation() {
		return op;
	}
	public String getAccountID() {
		return accountID;
	}
}
