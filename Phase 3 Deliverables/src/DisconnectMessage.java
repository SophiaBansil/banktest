
public class DisconnectMessage extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DisconnectMessage(SessionInfo session) {
        super(TYPE.DISCONNECT, session); 
    }
}
