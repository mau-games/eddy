package util.eventrouting.events;

import util.eventrouting.PCGEvent;

public class StartWorld extends PCGEvent{
	private int size;
	
	public StartWorld(int size) {
		this.setSize(size);
	}

	public int getSize() {
		return size;
	}

	private void setSize(int size) {
		this.size = size;
	}
}
