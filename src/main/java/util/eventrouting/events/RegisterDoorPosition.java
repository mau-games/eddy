package util.eventrouting.events;

import game.Room;
import util.Point;
import util.eventrouting.PCGEvent;

public class RegisterDoorPosition extends PCGEvent
{
	public Room room;
	
	public RegisterDoorPosition(Point doorPosition, Room room)
	{
		this.room = room;
		setPayload(doorPosition);
	}
}
