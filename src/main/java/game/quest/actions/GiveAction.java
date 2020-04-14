package game.quest.actions;

import finder.geometry.Point;
import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class GiveAction extends Action {
    private Point secondPosition;

    public GiveAction() {
        this.setType(ActionType.GIVE);

    }

    public GiveAction(boolean precondition) {
        super(precondition);
    }

    public GiveAction(ActionType type) {
        super(type);
    }
    public Point getSecondPosition() {
        return secondPosition;
    }

    public void setSecondPosition(Point secondPosition) {
        this.secondPosition = secondPosition;
    }
}
