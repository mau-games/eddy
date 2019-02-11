package util.eventrouting.events;

import game.Room;
import util.Point;
import util.eventrouting.ImportantRoomEvent;

public class InitialRoom extends ImportantRoomEvent
{

	public InitialRoom(Room pickedRoom, Point pos) {
		super(pickedRoom, pos);
		// TODO Auto-generated constructor stub
	}

}