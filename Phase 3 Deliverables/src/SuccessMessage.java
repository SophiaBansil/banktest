public class SuccessMessage extends Message {
    private static final long serialVersionUID = 1L;
    private final String message;
    
    public SuccessMessage(String message) {
        super(TYPE.SUCCESS, null);
        this.message = message;
    }
    
    public SuccessMessage(String message, SessionInfo session) {
        super(TYPE.SUCCESS, session);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
