package gui.utils;

import game.Room;
import util.Point;
        import game.Room;
        import util.Point;
        import util.eventrouting.EventRouter;
        import util.eventrouting.events.QuestPositionUpdate;

public class NarrativeEntityPositionBrush extends ShapeBrush {
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

    }

    @Override
    public void onReleaseRoom(Room releasedRoom, Point releasedPosition) {
        //not used
    }

}