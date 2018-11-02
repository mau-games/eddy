package util.eventrouting.events;

import game.Dungeon;
import game.MapContainer;
import game.Room;
import util.eventrouting.PCGEvent;

/*
* @author Chelsi Nolasco, Malmö University
* @author Axel Österman, Malmö University
*/

public class RequestRoomRemoval extends PCGEvent {
	
	/**
	 * Creates a new event.
	 * 
	 * @param payload The map to be worked on.
	 */

	public RequestRoomRemoval(Room payload, Dungeon currentDungeon, int dungeonID) 
	{
		setPayload(payload);
	}

}
