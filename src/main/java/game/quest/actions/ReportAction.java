package game.quest.actions;

import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class ReportAction extends Action {
    public ReportAction() {
    }

    public ReportAction(boolean precondition) {
        super(precondition);
    }

    public ReportAction(ActionType type) {
        super(type);
    }
}
