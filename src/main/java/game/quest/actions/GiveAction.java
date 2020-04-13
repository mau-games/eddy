package game.quest.actions;

import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class GiveAction extends Action {
    public GiveAction() {
    }

    public GiveAction(boolean precondition) {
        super(precondition);
    }

    public GiveAction(ActionType type) {
        super(type);
    }
}
