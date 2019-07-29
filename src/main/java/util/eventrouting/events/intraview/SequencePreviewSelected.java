package util.eventrouting.events.intraview;

import game.Room;
import util.eventrouting.IntraViewEvent;

public class SequencePreviewSelected extends IntraViewEvent 
{
	public SequencePreviewSelected()
	{
	}
	
	public SequencePreviewSelected(Room dungeonRoom)
	{
		setPayload(dungeonRoom);
	}
}
