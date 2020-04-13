package game.quest.actions;

import finder.geometry.Point;
import game.quest.Action;
import game.quest.ActionType;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class ExchangeAction extends Action {
    private Point secondPosition;
    public ExchangeAction() {
    }

    public ExchangeAction(boolean precondition) {
        super(precondition);
    }

    public ExchangeAction(ActionType type) {
        super(type);
    }

    public Point getSecondPosition() {
        return secondPosition;
    }

    public void setSecondPosition(Point secondPosition) {
        this.secondPosition = secondPosition;
    }
}
