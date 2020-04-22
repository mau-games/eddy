package game.quest.actions;

import finder.geometry.Point;
import game.Room;
import game.quest.Action;
import game.quest.ActionType;
import game.quest.ActionWithSecondPosition;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class TakeAction extends ActionWithSecondPosition {
    private Point secondPosition;
    private Room secondRoom;
    public TakeAction() {
        this.setType(ActionType.TAKE);

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

    @Override
    public Room getSecondRoom() {
        return secondRoom;
    }

    @Override
    public void setSecondRoom(Room room) {
        this.secondRoom = room;
    }
}
