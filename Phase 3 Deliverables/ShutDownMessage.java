public class ShutDownMessage extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ShutDownMessage() {
        super(TYPE.SHUTDOWN, null); 
    }
}
