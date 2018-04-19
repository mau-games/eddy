package util.eventrouting.events;

import util.eventrouting.PCGEvent;

public class ApplySuggestion extends PCGEvent{
	
	private int mapNumber;
	
	public  ApplySuggestion(int mapNumber ) {
		this.setMapNumber(mapNumber);
		setPayload(mapNumber);
	}

	private int getMapNumber() {
		return mapNumber;
	}

	private void setMapNumber(int mapNumber) {
		this.mapNumber = mapNumber;
	}

}
