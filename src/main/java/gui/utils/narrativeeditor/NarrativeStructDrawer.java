package gui.utils.narrativeeditor;

import util.eventrouting.EventRouter;
import util.eventrouting.events.ChangeCursor;

public class NarrativeStructDrawer
{
	private static NarrativeStructDrawer instance = null;
	
	protected AbstractNarrativeStructBrush brush;
//	protected List<ShapeBrush> allBrushes
	
	public enum NarrativeStructBrushesType
	{
		GRAMMAR_NODE_MOVEMENT,
		GRAMMAR_NODE_CONNECTOR
	}
	
	public NarrativeStructBrushesType narrative_brush_type;
	
	private NarrativeStructDrawer()
	{
		brush = new MoveNarrativeNodeBrush();
		narrative_brush_type = NarrativeStructBrushesType.GRAMMAR_NODE_MOVEMENT;
	}
	
	public static NarrativeStructDrawer getInstance()
	{
		if(instance == null)
		{
			instance = new NarrativeStructDrawer();
		}
		
		return instance;
	}
	
	public void changeBrushTo(NarrativeStructBrushesType difBrush, int connection_type)
	{
		narrative_brush_type = difBrush;
		changeBrush(connection_type);
	}
	
//	public void changeToConnector()
//	{
//		brush = new NarrativeStructConnectorBrush();
//	}
	
	public void changeToMove()
	{
		brush = new MoveNarrativeNodeBrush();
	}
	
	public AbstractNarrativeStructBrush getBrush()
	{
		return brush;
	}
	
	private void changeBrush(int connection_type)
	{
		EventRouter.getInstance().postEvent(new ChangeCursor(""));
		switch(narrative_brush_type)
		{
			case GRAMMAR_NODE_MOVEMENT:
				brush = new MoveNarrativeNodeBrush();
				break;
			case GRAMMAR_NODE_CONNECTOR:
				brush = new NarrativeStructConnectorBrush(connection_type);
				break;
		}
	}

	public void done()
	{
		brush = new DummyNarrativeStructBrush();
	}
}
