package src2.common;
import java.io.Serializable;

public abstract class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	public enum TYPE {
		LOGIN_CLIENT,
		LOGIN_TELLER,
		LOGOUT_ATM,
	    LOGOUT_CLIENT,
	    LOGOUT_TELLER,
	    SUCCESS,
	    FAILURE,
	    LOAD_ACCOUNT,
	    CREATE_ACCOUNT,
	    SAVE_ACCOUNT,
	    SHARE_ACCOUNT,
	    DELETE_ACCOUNT,
	    LOAD_PROFILE,
	    SAVE_PROFILE,
	    CREATE_PROFILE,
	    DELETE_PROFILE,
	    EXIT_ACCOUNT,
	    EXIT_PROFILE,
	    TRANSACTION,
	    SHUTDOWN, 
		DISCONNECT
	}
	
	private final TYPE type;
    private final SessionInfo session;

    protected Message(TYPE type, SessionInfo session) {
        this.type = type;
        this.session = session;
    }

    public TYPE getType() {
        return type;
    }

    public SessionInfo getSession() {
        return session;
    }
}
