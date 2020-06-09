package game.quest.actions;

import game.Tile;
import game.quest.Action;
import game.quest.ActionType;

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
        Tile tile = getRoom().getTile(getPosition().getX(),getPosition().getY());
        setPrecondition(
                tile.GetType().isEnemyBoss() ||
                tile.GetType().isEnemy() ||
                tile.GetType().isNPC()
        );
    }
}
