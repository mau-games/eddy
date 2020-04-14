package game.quest.actions;

import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class UseAction extends Action {
    public UseAction() {
        this.setType(ActionType.USE);

    }

    public UseAction(boolean precondition) {
        super(precondition);
    }

    public UseAction(ActionType type) {
        super(type);
    }
}
