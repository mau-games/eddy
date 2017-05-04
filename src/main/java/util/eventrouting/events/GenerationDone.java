package util.eventrouting.events;

/**
 * This event is used to post generation data.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class GenerationDone extends AlgorithmEvent {
	
	/**
	 * Creates a new event.
	 * 
	 * @param payload The message to be sent.
	 */
	public GenerationDone(String payload) {
		setPayload(payload);
	}
}
