package game.quest.actions;

import game.Tile;
import game.quest.Action;
import game.quest.ActionType;

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
}

