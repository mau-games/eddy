package util.eventrouting.events.intraview;

import game.Room;
import util.eventrouting.IntraViewEvent;

public class DungeonPreviewSelected extends IntraViewEvent 
{
	public DungeonPreviewSelected(Room dungeonRoom)
	{
		setPayload(dungeonRoom);
	}
}
