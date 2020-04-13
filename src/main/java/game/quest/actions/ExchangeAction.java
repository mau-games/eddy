package game.quest.actions;

import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class ExchangeAction extends Action {
    public ExchangeAction() {
    }

    public ExchangeAction(boolean precondition) {
        super(precondition);
    }

    public ExchangeAction(ActionType type) {
        super(type);
    }
}
