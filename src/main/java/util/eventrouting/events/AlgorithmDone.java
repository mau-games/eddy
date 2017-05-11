package util.eventrouting.events;

import game.MapContainer;

/**
 * This event is triggered when an algorithm run is completed.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class AlgorithmDone extends AlgorithmEvent {
	
	public AlgorithmDone(MapContainer map) {
		setPayload(map);
	}
}
