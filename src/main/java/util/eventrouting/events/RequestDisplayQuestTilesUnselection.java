package util.eventrouting.events;

import util.eventrouting.PCGEvent;

public class RequestDisplayQuestTilesUnselection extends PCGEvent {
    public RequestDisplayQuestTilesUnselection(boolean doublePosition) {
        setPayload(doublePosition);
    }
}
