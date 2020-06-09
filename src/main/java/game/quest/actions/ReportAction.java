package game.quest.actions;

import game.Tile;
import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class ReportAction extends Action {
    public ReportAction() {
        this.setType(ActionType.REPORT);
    }

    public ReportAction(boolean precondition) {
        super(precondition);
    }

    public ReportAction(ActionType type) {
        super(type);
    }

    @Override
    public void checkConditions() {
        Tile tile = getRoom().getTile(getPosition().getX(),getPosition().getY());
        setPrecondition(tile.GetType().isNPC());
    }
}

