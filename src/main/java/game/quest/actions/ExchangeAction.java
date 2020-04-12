package game.quest.actions;

import finder.geometry.Point;
import game.quest.Action;
/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class ExchangeAction extends Action {
    private Point secondPosition;

    public Point getSecondPosition() {
        return secondPosition;
    }

    public void setSecondPosition(Point secondPosition) {
        this.secondPosition = secondPosition;
    }
}
