package game.quest.actions;

import game.Tile;
import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class DefendAction extends Action {
    public DefendAction() {
        this.setType(ActionType.DEFEND);

    }

    public DefendAction(boolean precondition) {
        super(precondition);
    }

    public DefendAction(ActionType type) {
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

