
public final class ShareAccountMessage extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String targetProfileUsername;
	private final String sharedAccountID;
	public ShareAccountMessage(TYPE type, SessionInfo session, String targetProfile, String shareAccountID) {
		super(type, session);
		
		this.targetProfileUsername = targetProfile;
		this.sharedAccountID = shareAccountID;
	}
	
	public String getTargetProfile() {
		return this.targetProfileUsername;
	}
	
	public String getSharedAccountID() {
		return this.sharedAccountID;
	}
}
