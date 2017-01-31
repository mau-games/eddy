package util.config;

public class MissingConfigurationException extends Exception {
	
	/**
	 * Creates an exception denoting that there is currently no valid
	 * configuration being in use.
	 */
	public MissingConfigurationException() {
		super("No configuration set. Either you haven't loaded one yet, or the specified profile does not exist.");
	}
	
	/**
	 * Creates an exception denoting that there is currently no valid
	 * configuration being in use.
	 * 
	 * @param message A lovely little message to convey to your fellow
	 * 		developer or unhappy user.
	 */
	public MissingConfigurationException(String message) {
		super(message);
	}
}
