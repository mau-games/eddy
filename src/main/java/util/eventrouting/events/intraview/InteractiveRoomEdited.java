package util.eventrouting.events.intraview;

import game.Room;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import util.eventrouting.IntraViewEvent;
import gui.controls.*;

public class InteractiveRoomEdited extends IntraViewEvent
{
    public Room interactiveRoom;
    public InteractiveMap editedCanvas;
    public ImageView tileToChange;
    public MouseEvent mouseEvent;

    public InteractiveRoomEdited(InteractiveMap editedCanvas, Room interactiveRoom, ImageView tileToChange, MouseEvent mouseEvent)
    {
        this.editedCanvas = editedCanvas;
        this.interactiveRoom = interactiveRoom;
        this.tileToChange = tileToChange;
        this.mouseEvent = mouseEvent;
    }
}
