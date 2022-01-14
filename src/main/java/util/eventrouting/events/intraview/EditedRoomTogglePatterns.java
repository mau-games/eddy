package util.eventrouting.events.intraview;

import util.eventrouting.IntraViewEvent;

public class EditedRoomTogglePatterns extends IntraViewEvent {

    public boolean toggle;

    public EditedRoomTogglePatterns(boolean toggle)
    {
        this.toggle = toggle;
    }
}
