package gui.utils.narrativeeditor;

import game.Room;
import game.narrative.GrammarNode;
import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.events.RequestConnection;
import util.eventrouting.events.RequestConnectionGrammarStructureGraph;

public class NarrativeStructConnectorBrush extends InterNarrativeStructBrush
{
	public GrammarNode from;
	public GrammarNode to;
	public int connection_type;
	
	private GrammarNode aux;
	//maybe it can have info about painting? this is in another part now (RoomEdgeLine)
	
	public NarrativeStructConnectorBrush(int connection_type)
	{
		this.connection_type = connection_type;
	}

	@Override
	public void onEnteredGrammarNode(GrammarNode enteredGrammarNode)
	{
		
	}

	@Override
	public void onClickGrammarNode(GrammarNode clickedGrammarNode)
	{
		if(from == null)
		{
			this.from = clickedGrammarNode;
		}
		else if(from != clickedGrammarNode)
		{
			System.out.println("CONNECTIONS!");
			this.to = clickedGrammarNode;
			EventRouter.getInstance().postEvent(new RequestConnectionGrammarStructureGraph(from, to, connection_type));
			from = null;
			to = null;
		}
	}

	@Override
	public void onReleaseGrammarNode(GrammarNode releasedGrammarNode)
	{
	}
	
	
	public void InitDrawing()
	{
		
	}
	
	public void EndDrawing()
	{
		
	}
}
