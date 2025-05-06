package src2.common;
import src2.common.Message.TYPE;

public class DisconnectMessage extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DisconnectMessage(SessionInfo session) {
        super(TYPE.DISCONNECT, session); 
    }
}
