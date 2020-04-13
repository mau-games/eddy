package game.quest.actions;

import finder.geometry.Point;
import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class TakeAction extends Action {
    private Point secondPosition;
    public TakeAction() {
    }

    public TakeAction(boolean precondition) {
        super(precondition);
    }

    public TakeAction(ActionType type) {
        super(type);
    }

    public Point getSecondPosition() {
        return secondPosition;
    }

    public void setSecondPosition(Point secondPosition) {
        this.secondPosition = secondPosition;
    }
}
