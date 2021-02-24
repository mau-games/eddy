package util.eventrouting.events;

import game.Dungeon;
import game.RoomEdge;
import game.narrative.NarrativeShapeEdge;
import util.eventrouting.PCGEvent;

/***
 * was it me?
 * @author Alberto Alvarez, Malmö University
 *
 */
public class RequestGrammarNodeConnectionRemoval extends PCGEvent
{
	public RequestGrammarNodeConnectionRemoval(NarrativeShapeEdge currentEdge)
	{
		setPayload(currentEdge);
	}
}
