package util.eventrouting.events.intraview;

import game.Room;
import util.eventrouting.IntraViewEvent;

public class LoadedXMLRoomSelected extends IntraViewEvent{
	
	public LoadedXMLRoomSelected()
	{
	}
	
	public LoadedXMLRoomSelected(Room xmlRoom)
	{
		setPayload(xmlRoom);
	}
}
