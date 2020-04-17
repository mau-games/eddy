package game.quest.actions;

import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class StealthAction extends Action {
    public StealthAction() {
        this.setType(ActionType.STEALTH);

    }

    public StealthAction(boolean precondition) {
        super(precondition);
    }

    public StealthAction(ActionType type) {
        super(type);
    }
}

