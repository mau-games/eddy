package util.eventrouting.events;

import game.Dungeon;
import game.Room;
import game.narrative.GrammarNode;
import util.eventrouting.PCGEvent;

public class RequestGrammarStructureNodeRemoval extends PCGEvent {

	/**
	 * Creates a new event.
	 *
	 * @param payload The map to be worked on.
	 */

	public RequestGrammarStructureNodeRemoval(GrammarNode nodeToRemove)
	{
		setPayload(nodeToRemove);
	}

}
