package util.eventrouting.events;

import finder.geometry.Point;
import game.Room;
import util.eventrouting.PCGEvent;

public class EntityPositionUpdate extends PCGEvent {

        private final Point point;
        private final Room room;
        private boolean secondPosition;

        public EntityPositionUpdate(Point point, Room room, boolean doublePos) {
            super();
            this.point = point;
            this.room = room;
            this.secondPosition = doublePos;
        }

        public Point getPoint() {
            return point;
        }

        public Room getRoom() {
            return room;
        }

        public boolean isSecondPosition() {
            return secondPosition;
        }
}
