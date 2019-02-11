package util.eventrouting.events;

import game.Dungeon;
import game.RoomEdge;
import util.eventrouting.PCGEvent;

/***
 * was it me?
 * @author Alberto Alvarez, Malmö University
 *
 */
public class RequestConnectionRemoval extends PCGEvent
{
	public RequestConnectionRemoval(RoomEdge currentEdge, Dungeon currentDungeon, int dungeonID)
	{
		setPayload(currentEdge);
	}
}
