package util.eventrouting.events.intraview;

import gui.controls.Drawer;
import javafx.scene.Cursor;
import util.eventrouting.IntraViewEvent;

public class InteractiveRoomBrushUpdated extends IntraViewEvent
{
    public Cursor new_cursor;
    public Drawer brush;

    public InteractiveRoomBrushUpdated(Drawer brush, Cursor new_cursor)
    {
        this.brush = brush;
        this.new_cursor = new_cursor;

    }

}
