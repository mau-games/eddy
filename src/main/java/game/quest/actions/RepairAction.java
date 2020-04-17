package game.quest.actions;

import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class RepairAction extends Action {
    public RepairAction() {
        this.setType(ActionType.REPAIR);

    }

    public RepairAction(boolean precondition) {
        super(precondition);
    }

    public RepairAction(ActionType type) {
        super(type);
    }
}

