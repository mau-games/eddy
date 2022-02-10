package util.eventrouting.events;

import game.Room;
import util.eventrouting.PCGEvent;

public class RoomEdited extends PCGEvent {

	public Room editedRoom;

	public RoomEdited(Room editedRoom) 
	{
		setPayload(editedRoom);
		this.editedRoom = editedRoom;
	}

}
