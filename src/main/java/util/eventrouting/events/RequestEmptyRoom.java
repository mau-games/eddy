package util.eventrouting.events;

import game.MapContainer;
import util.eventrouting.PCGEvent;

public class RequestEmptyRoom extends PCGEvent {
	
	/**
	 * Creates a new event.
	 * 
	 * @param payload The map to be worked on.
	 */
	public RequestEmptyRoom(MapContainer payload) {
		setPayload(payload);
	}
	
}
