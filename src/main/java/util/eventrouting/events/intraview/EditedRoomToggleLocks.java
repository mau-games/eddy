package util.eventrouting.events.intraview;

import util.eventrouting.IntraViewEvent;

public class EditedRoomToggleLocks extends IntraViewEvent {

    public boolean toggle;

    public EditedRoomToggleLocks(boolean toggle)
    {
        this.toggle = toggle;
    }
}
