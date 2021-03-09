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
public class ReadAction extends Action {
    public ReadAction() {
        this.setType(ActionType.READ);

    }

    public ReadAction(boolean precondition) {
        super(precondition);
    }

    public ReadAction(ActionType type) {
        super(type);
    }

    @Override
    public void checkConditions() {
        Tile tile = getRoom().getTile(getPosition().getX(),getPosition().getY());
        setPrecondition(tile.GetType().isItem());
    }
    @Override
    public List<QuestMotives> CheckMotives()
    {
    	List<QuestMotives> tempList = new ArrayList<QuestMotives>();
    	tempList.add(QuestMotives.CONQUEST);
    	return tempList;
    }
}

