package util.eventrouting.events;

import util.eventrouting.PCGEvent;

/**
 * This event is used to post status messages.
 * 
 * @author Alexander Baldwin, Malmö University
 * @author Johan Holmberg, Malmö University
 */
public class StatusMessage extends PCGEvent {
	
	/**
	 * Creates a new event.
	 * 
	 * @param payload The message to be sent.
	 */
	public StatusMessage(String payload) {
		setPayload(payload);
	}
}
