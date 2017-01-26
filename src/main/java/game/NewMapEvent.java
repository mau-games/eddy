package game;

import util.eventrouting.PCGEvent;

/**
 * This event is used to post info on new maps.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class NewMapEvent extends PCGEvent {
	
	/**
	 * Creates a new event.
	 * 
	 * @param payload The map to be sent.
	 */
	public NewMapEvent(Map payload) {
		setPayload(payload);
	}
}
