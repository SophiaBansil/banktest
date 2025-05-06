package src2.common;
import java.math.BigDecimal;
import java.util.List;

public class CheckingAccount extends Account {
	public CheckingAccount() {
		super();
	}

	// constructor for client-side refresh
	public CheckingAccount(String id, BigDecimal balance,  List<Transaction> history) {
		super(id, balance, history);
	}
}
