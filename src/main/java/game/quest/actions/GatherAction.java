package game.quest.actions;

import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class GatherAction extends Action {
    public GatherAction() {
    }

    public GatherAction(boolean precondition) {
        super(precondition);
    }

    public GatherAction(ActionType type) {
        super(type);
    }
}
