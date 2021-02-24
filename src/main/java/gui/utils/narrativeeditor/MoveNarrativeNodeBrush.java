package gui.utils.narrativeeditor;

import game.Room;
import game.narrative.GrammarNode;
import javafx.scene.Cursor;
import util.Point;

public class MoveNarrativeNodeBrush extends AbstractNarrativeStructBrush
{
	Cursor cursorType;
	
	public MoveNarrativeNodeBrush()
	{
		cursorType = Cursor.MOVE;
	}

	@Override
	public void onEnteredGrammarNode(GrammarNode enteredGrammarNode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClickGrammarNode(GrammarNode clickedGrammarNode) {

	}

	@Override
	public void onReleaseGrammarNode(GrammarNode releasedGrammarNode) {

	}

}
