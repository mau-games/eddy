package util.eventrouting.events;

import java.util.HashMap;

import util.eventrouting.PCGEvent;

/**
 * This event is triggered when an algorithm run is completed.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class AlgorithmDone extends AlgorithmEvent {
	
	public AlgorithmDone(HashMap<String, Object> map) {
		setPayload(map);
	}
}
