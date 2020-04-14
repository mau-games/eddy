package game.quest.actions;

import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class DamageAction extends Action {
    public DamageAction() {
        this.setType(ActionType.DAMAGE);

    }

    public DamageAction(boolean precondition) {
        super(precondition);
    }

    public DamageAction(ActionType type) {
        super(type);
    }
}
