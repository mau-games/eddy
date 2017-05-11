package util.eventrouting.events;

import game.MapContainer;
import util.eventrouting.PCGEvent;
/**
 * This event is used to request a view switch.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class RequestViewSwitch extends PCGEvent{
	
	/**
	 * Creates a new event.
	 * 
	 * @param payload The map to be worked on.
	 */
	public RequestViewSwitch(MapContainer payload) {
		setPayload(payload);
	}

}
