package game.quest.actions;

import game.Tile;
import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class DamageAction extends Action {
    public DamageAction() {
        this.setType(ActionType.DAMAGE);

    }

    public DamageAction(boolean precondition) {
        super(precondition);
    }

    public DamageAction(ActionType type) {
        super(type);
    }

    @Override
    public void checkConditions() {
        Tile tile = getRoom().getTile(getPosition().getX(),getPosition().getY());
        setPrecondition(
                tile.GetType().isItem() ||
                tile.GetType().isNPC()
        );
    }
}

