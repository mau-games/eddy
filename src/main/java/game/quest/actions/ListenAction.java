package game.quest.actions;

import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class ListenAction extends Action {
    public ListenAction() {
    }

    public ListenAction(boolean precondition) {
        super(precondition);
    }

    public ListenAction(ActionType type) {
        super(type);
    }
}
