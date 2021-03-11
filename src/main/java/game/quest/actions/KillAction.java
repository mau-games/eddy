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
public class KillAction extends Action {
    public KillAction() {
        this.setType(ActionType.KILL);

    }

    public KillAction(boolean precondition) {
        super(precondition);
    }

    public KillAction(ActionType type) {
        super(type);
    }

    @Override
    public void checkConditions() {
        Tile tile = getRoom().getTile(getPosition().getX(),getPosition().getY());
        setPrecondition(
                tile.GetType().isEnemyBoss() ||
                        tile.GetType().isEnemy() ||
                        tile.used == false
                        
        );
    }
}

