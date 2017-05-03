package util.eventrouting.events;

import javafx.scene.image.Image;
import util.eventrouting.PCGEvent;

/**
 * This event is used to post status messages.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class MapRendered extends AlgorithmEvent {
	
	/**
	 * Creates a new event.
	 * 
	 * @param payload The message to be sent.
	 */
	public MapRendered(Image payload) {
		setPayload(payload);
	}
}
