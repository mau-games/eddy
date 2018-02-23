package util.eventrouting.events;

import util.eventrouting.PCGEvent;

public class RequestSuggestionsView extends PCGEvent {

	private int threads;

	
	public RequestSuggestionsView() {
		threads = 1;
	}
	
	public RequestSuggestionsView(int threads) {
		this.threads = threads;
	}
	
	public int getNbrOfThreads() {
		return threads;
	}
}
