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
	    LOAD_PROFILE,
	    SAVE_PROFILE,
	    SAVE_ACCOUNT,
	    SHARE_ACCOUNT,
	    DELETE_ACCOUNT,
	    DELETE_PROFILE,
	    TRANSACTION,
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
