package util.eventrouting.events;

import java.util.UUID;

import util.eventrouting.PCGEvent;

public class AlgorithmEvent extends PCGEvent {

	protected UUID algorithmID = null;
	
	public UUID getID(){
		return algorithmID;
	}
	
	public void setID(UUID id){
		algorithmID = id;
	}
}
