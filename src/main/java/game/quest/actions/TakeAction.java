package game.quest.actions;

import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class TakeAction extends Action {
    public TakeAction() {
    }

    public TakeAction(boolean precondition) {
        super(precondition);
    }

    public TakeAction(ActionType type) {
        super(type);
    }
}
