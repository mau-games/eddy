package util.eventrouting;

/**
 * This is the base class for events within the system. It should be extended
 * in order to be more efficiently routed.
 * 
 * @author Johan Holmberg, Malmö University
 */
public abstract class PCGEvent {
	private Object payload;
	
	/**
	 * Sets the payload object.
	 * 
	 * @param payload An object.
	 */
	public void setPayload(Object payload) {
		this.payload = payload;
	}
	
	/**
	 * Gets the payload from this event.
	 * 
	 * @return An object.
	 */
	public Object getPayload() {
		return payload;
	}
}
