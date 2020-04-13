package game.quest.actions;

import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class EscortAction extends Action {

    public EscortAction() {
    }

    public EscortAction(boolean precondition) {
        super(precondition);
    }

    public EscortAction(ActionType type) {
        super(type);
    }
}
