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
public class GiveAction extends ActionWithSecondPosition {
    private Point secondPosition;
    private Room secondRoom;

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

    @Override
    public Room getSecondRoom() {
        return secondRoom;
    }

    @Override
    public void setSecondRoom(Room room) {
        this.secondRoom = room;
    }
}

