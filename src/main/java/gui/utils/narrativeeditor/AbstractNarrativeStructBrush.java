package gui.utils.narrativeeditor;

import game.Room;
import game.narrative.GrammarNode;
import util.Point;

public abstract class AbstractNarrativeStructBrush
{
	public AbstractNarrativeStructBrush()
	{
		
	}
	
	public abstract void onEnteredGrammarNode(GrammarNode enteredGrammarNode);
	public abstract void onClickGrammarNode(GrammarNode clickedGrammarNode);
	public abstract void onReleaseGrammarNode(GrammarNode releasedGrammarNode);
}
