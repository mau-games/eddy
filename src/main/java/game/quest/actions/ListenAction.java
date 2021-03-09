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
public class ListenAction extends Action {
    public ListenAction() {
        this.setType(ActionType.LISTEN);

    }

    public ListenAction(boolean precondition) {
        super(precondition);
    }

    public ListenAction(ActionType type) {
        super(type);
    }

    @Override
    public void checkConditions() {
        Tile tile = getRoom().getTile(getPosition().getX(),getPosition().getY());
        setPrecondition(tile.GetType().isNPC());
    }
    @Override
    public List<QuestMotives> CheckMotives()
    {
    	List<QuestMotives> tempList = new ArrayList<QuestMotives>();
    	tempList.add(QuestMotives.CONQUEST);
    	return tempList;
    }
}

