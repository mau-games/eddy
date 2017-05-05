package util.eventrouting.events;

import game.MapContainer;

/**
 * This event is used to post status messages.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class MapLoaded extends AlgorithmEvent {
	
	/**
	 * Creates a new event.
	 * 
	 * @param payload The message to be sent.
	 */
	public MapLoaded(MapContainer payload) {
		setPayload(payload);
	}
}
