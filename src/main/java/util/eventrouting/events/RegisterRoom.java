package util.eventrouting.events;

import game.Room;
import util.eventrouting.PCGEvent;

public class RegisterRoom extends PCGEvent
{
	public RegisterRoom(Room currentRoom)
	{
		setPayload(currentRoom);
	}
}
