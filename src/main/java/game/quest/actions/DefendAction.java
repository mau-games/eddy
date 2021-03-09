package game.quest.actions;

import java.util.ArrayList;
import java.util.List;

import game.Tile;
import game.quest.Action;
import game.quest.ActionType;
import generator.algorithm.grammar.QuestGrammar.QuestMotives;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class DefendAction extends Action {
    public DefendAction() {
        this.setType(ActionType.DEFEND);

    }

    public DefendAction(boolean precondition) {
        super(precondition);
    }

    public DefendAction(ActionType type) {
        super(type);
    }

    @Override
    public void checkConditions() {
        Tile tile = getRoom().getTile(getPosition().getX(),getPosition().getY());
        setPrecondition(
                tile.GetType().isItem() ||
                        tile.GetType().isNPC()
        );
    }
    @Override
    public List<QuestMotives> CheckMotives()
    {
    	List<QuestMotives> tempList = new ArrayList<QuestMotives>();
    	tempList.add(QuestMotives.CONQUEST);
    	return tempList;
    }
}

