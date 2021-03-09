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
public class ExperimentAction extends Action {
    public ExperimentAction() {
        this.setType(ActionType.EXPERIMENT);

    }

    public ExperimentAction(boolean precondition) {
        super(precondition);
    }

    public ExperimentAction(ActionType type) {
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
