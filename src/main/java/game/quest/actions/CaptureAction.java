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
public class CaptureAction extends Action {
    public CaptureAction() {
        this.setType(ActionType.CAPTURE);
    }
    public CaptureAction(boolean precondition) {
        super(precondition);
    }

    public CaptureAction(ActionType type) {
        super(type);
    }

    @Override
    public void checkConditions() {
        getRoom().isIntraFeasible();
        Tile tile = getRoom().getTile(getPosition().getX(),getPosition().getY());
        setPrecondition(
                getRoom().walkablePositions.stream().anyMatch(walkablePosition ->
                        walkablePosition.getPoint().getX() == getPosition().getX() &&
                        walkablePosition.getPoint().getY() == getPosition().getY()) &&
                        (tile.GetType().isEnemyBoss() ||
                        tile.GetType().isEnemy() ||
                        tile.GetType().isNPC())
        );
    }
}
