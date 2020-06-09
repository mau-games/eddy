package game.quest.actions;

import game.Tile;
import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class GatherAction extends Action {
    public GatherAction() {
        this.setType(ActionType.GATHER);

    }

    public GatherAction(boolean precondition) {
        super(precondition);
    }

    public GatherAction(ActionType type) {
        super(type);
    }

    @Override
    public void checkConditions() {
        Tile tile = getRoom().getTile(getPosition().getX(),getPosition().getY());
        setPrecondition(tile.GetType().isItem());
    }
}

