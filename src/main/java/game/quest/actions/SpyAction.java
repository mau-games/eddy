package game.quest.actions;

import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class SpyAction extends Action {
    public SpyAction() {
        this.setType(ActionType.SPY);

    }

    public SpyAction(boolean precondition) {
        super(precondition);
    }

    public SpyAction(ActionType type) {
        super(type);
    }
}
