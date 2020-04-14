package game.quest.actions;

import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class ExperimentAction extends Action {
    public ExperimentAction() {
        this.setType(ActionType.EXPERIMENT);

    }

    public ExperimentAction(boolean precondition) {
        super(precondition);
    }

    public ExperimentAction(ActionType type) {
        super(type);
    }
}
