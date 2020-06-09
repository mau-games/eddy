package game.quest.actions;

import game.Tile;
import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class UseAction extends Action {
    public UseAction() {
        this.setType(ActionType.USE);

    }

    public UseAction(boolean precondition) {
        super(precondition);
    }

    public UseAction(ActionType type) {
        super(type);
    }

    @Override
    public void checkConditions() {
        Tile tile = getRoom().getTile(getPosition().getX(),getPosition().getY());
        setPrecondition(tile.GetType().isItem());
    }
}