package game.quest.actions;

import game.Tile;
import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class RepairAction extends Action {
    public RepairAction() {
        this.setType(ActionType.REPAIR);

    }

    public RepairAction(boolean precondition) {
        super(precondition);
    }

    public RepairAction(ActionType type) {
        super(type);
    }

    @Override
    public void checkConditions() {
        Tile tile = getRoom().getTile(getPosition().getX(),getPosition().getY());
        setPrecondition(tile.GetType().isItem());
    }
}

