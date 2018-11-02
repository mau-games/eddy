package util.eventrouting.events;

import game.MapContainer;
import util.eventrouting.PCGEvent;

/*
* @author Chelsi Nolasco, Malmö University
* @author Axel Österman, Malmö University
*/
//TODO: MAYBE SEND THE DUNGEON!!
public class RequestSuggestionsView extends PCGEvent {
	private int threads;


	public RequestSuggestionsView() {
		threads = 1;
	}

	public RequestSuggestionsView(MapContainer payload, int threads) {
		this.threads = threads;
		setPayload(payload);
	}

	public int getNbrOfThreads() {
		return threads;
	}
}






