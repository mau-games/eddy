package game.quest.actions;

import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class ExploreAction extends Action {
    public ExploreAction() {
    }

    public ExploreAction(boolean precondition) {
        super(precondition);
    }

    public ExploreAction(ActionType type) {
        super(type);
    }
}
