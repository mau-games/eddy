package gui.utils;

import game.Room;
import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.events.QuestPositionUpdate;

public class QuestPositionBrush extends ShapeBrush {
    public Room room;
    public Point position;

    @Override
    public void onEnteredRoom(Room enteredRoom) {
        //not used
    }

    @Override
    public void onClickRoom(Room clickedRoom, Point clickPosition) {
        this.room = clickedRoom;
        this.position = clickPosition;
        EventRouter.getInstance().postEvent(new QuestPositionUpdate(new finder.geometry.Point(position.getX(),position.getY()),room));
    }

    @Override
    public void onReleaseRoom(Room releasedRoom, Point releasedPosition) {
        //not used
    }

}
