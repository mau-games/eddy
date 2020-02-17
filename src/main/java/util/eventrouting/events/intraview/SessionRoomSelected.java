package util.eventrouting.events.intraview;

import game.Room;
import util.eventrouting.IntraViewEvent;

public class SessionRoomSelected extends IntraViewEvent {

	public SessionRoomSelected()
	{
	}
	
	public SessionRoomSelected(Room xmlRoom)
	{
		setPayload(xmlRoom);
	}
}
