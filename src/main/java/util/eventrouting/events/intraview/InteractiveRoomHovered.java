package util.eventrouting.events.intraview;

import game.Room;
import gui.controls.InteractiveMap;
import util.eventrouting.IntraViewEvent;
import javafx.scene.input.MouseEvent;

public class InteractiveRoomHovered extends IntraViewEvent
{
    public InteractiveMap editedCanvas;
    public Room interactiveRoom;
    public util.Point pointToChange;
    public MouseEvent mouseEvent;

    public InteractiveRoomHovered(InteractiveMap editedCanvas, Room interactiveRoom, util.Point pointToChange, MouseEvent mouseEvent)
    {
        this.editedCanvas = editedCanvas;
        this.interactiveRoom = interactiveRoom;
        this.pointToChange = pointToChange;
        this.mouseEvent = mouseEvent;
    }
}
