package util.eventrouting.events;

import gui.controls.ScaledRoom;
import util.eventrouting.PCGEvent;

public class ScaledMapSelected extends PCGEvent {
    public ScaledMapSelected(ScaledRoom scaledRoom){
        setPayload(scaledRoom);
    }
}
