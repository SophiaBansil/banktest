public class FailureMessage extends Message {
    private static final long serialVersionUID = 1L;
    private final String message;

    public FailureMessage(String message) {
        super(TYPE.SUCCESS, null); // You can define this enum in the Message class
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
