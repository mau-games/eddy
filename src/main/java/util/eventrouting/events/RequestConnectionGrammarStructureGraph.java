package util.eventrouting.events;

import game.Dungeon;
import game.Room;
import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;
import util.Point;
import util.eventrouting.PCGEvent;

//This event is only readable
public class RequestConnectionGrammarStructureGraph extends PCGEvent
{
	public GrammarNode from;
	public GrammarNode to;
	public int connection_type;

	public RequestConnectionGrammarStructureGraph(GrammarNode from, GrammarNode to, int connection_type)
	{
		this.from = from;
		this.to = to;
		this.connection_type = connection_type;
	}

}
