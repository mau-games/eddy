package game.quest.actions;

import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class ReadAction extends Action {
    public ReadAction() {
    }

    public ReadAction(boolean precondition) {
        super(precondition);
    }

    public ReadAction(ActionType type) {
        super(type);
    }
}
