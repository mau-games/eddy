package util.eventrouting.events;

import util.eventrouting.PCGEvent;

public class Start extends PCGEvent{
	private int threads;
	
	public Start() {
		threads = 1;
	}
	
	public Start(int threads) {
		this.threads = threads;
	}
	
	public int getNbrOfThreads() {
		return threads;
	}
}
