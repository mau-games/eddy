package util.eventrouting.events;

import java.util.HashMap;

import javafx.scene.image.Image;
import util.eventrouting.PCGEvent;

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
	public MapLoaded(HashMap<String, Object> payload) {
		setPayload(payload);
	}
}
