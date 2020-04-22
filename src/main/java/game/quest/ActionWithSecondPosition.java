package game.quest;

import finder.geometry.Point;
import game.Room;
//TODO: Rename this class
public abstract class ActionWithSecondPosition extends Action{
    public ActionWithSecondPosition(boolean precondition) {
        super(precondition);
    }

    public ActionWithSecondPosition(ActionType type) {
        super(type);
    }

    public ActionWithSecondPosition() {
        super();
    }

    public abstract Point getSecondPosition();

    public abstract void setSecondPosition(Point secondPosition);

    public abstract Room getSecondRoom();

    public abstract void setSecondRoom(Room room);
}
