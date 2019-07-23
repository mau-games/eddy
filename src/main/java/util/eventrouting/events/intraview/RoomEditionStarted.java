package util.eventrouting.events.intraview;

import game.Room;
import util.eventrouting.IntraViewEvent;

public class RoomEditionStarted extends IntraViewEvent
{
	public RoomEditionStarted(Room roomToBeEdited) 
	{
		setPayload(roomToBeEdited);
	}
}
