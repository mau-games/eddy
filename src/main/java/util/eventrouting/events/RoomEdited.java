package util.eventrouting.events;

import game.Room;
import util.eventrouting.PCGEvent;

public class RoomEdited extends PCGEvent {
	
	public RoomEdited(Room editedRoom) 
	{
		setPayload(editedRoom);
	}

}
