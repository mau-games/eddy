package util.eventrouting;

/**
 * This is the base class for events within the system. It should be extended
 * in order to be more efficiently routed.
 * 
 * @author Johan Holmberg, Malmö University
 */
public abstract class PCGEvent {
	private Object payload;
	private Object payload2;
	
	/**
	 * Sets the payload object.
	 * 
	 * @param payload An object.
	 */
	public void setPayload(Object payload) {
		this.payload = payload;
	}
	public void setPayload2(Object payload) {
		this.payload2 = payload;
	}
	/**
	 * Gets the payload from this event.
	 * 
	 * @return An object.
	 */
	public Object getPayload() {
		return payload;
	}
	public Object getPayload2() {
		return payload2;
	}
}
