package game.quest.actions;

import game.quest.Action;
import game.quest.ActionType;

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
}

