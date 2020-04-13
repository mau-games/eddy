package game.quest.actions;

import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class DefendAction extends Action {
    public DefendAction() {
    }

    public DefendAction(boolean precondition) {
        super(precondition);
    }

    public DefendAction(ActionType type) {
        super(type);
    }
}
