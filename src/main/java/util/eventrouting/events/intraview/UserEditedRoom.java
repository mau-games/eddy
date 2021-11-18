package util.eventrouting.events.intraview;

import game.Room;
import util.eventrouting.IntraViewEvent;

import java.util.UUID;

public class UserEditedRoom extends IntraViewEvent
{
    public UUID uniqueCanvasID;
    public Room editedRoom;

    public UserEditedRoom(UUID uniqueCanvasID, Room editedRoom)
    {
        this.uniqueCanvasID = uniqueCanvasID;
        this.editedRoom = editedRoom;
    }
}
