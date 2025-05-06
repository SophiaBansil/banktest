
package src2.common;
import src2.common.Message.TYPE;


public class ShutDownMessage extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ShutDownMessage() {
        super(TYPE.SHUTDOWN, null); 
    }
}
