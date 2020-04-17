package util.eventrouting.events;

import finder.geometry.Point;
import game.Room;
import util.eventrouting.PCGEvent;

public class QuestPositionUpdate extends PCGEvent {
    private final Point point;
    private final Room room;

    public QuestPositionUpdate(Point point, Room room) {
        super();
        this.point = point;
        this.room = room;
    }

    public Point getPoint() {
        return point;
    }

    public Room getRoom() {
        return room;
    }
}
