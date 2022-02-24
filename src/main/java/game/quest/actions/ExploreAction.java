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
public class ExploreAction extends Action {
    public ExploreAction() {
        this.setType(ActionType.EXPLORE);

    }

    public ExploreAction(boolean precondition) {
        super(precondition);
    }

    public ExploreAction(ActionType type) {
        super(type);
    }

    @Override
    public void checkConditions() {
        setPrecondition(getRoom().walkablePositions.stream().anyMatch(walkablePosition ->
                walkablePosition.getPoint().getX() == getPosition().getX() &&
                walkablePosition.getPoint().getY() == getPosition().getY()));
    }
}

