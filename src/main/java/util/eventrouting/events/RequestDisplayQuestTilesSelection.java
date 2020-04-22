package util.eventrouting.events;

import game.TileTypes;
import util.eventrouting.PCGEvent;

import java.util.List;

public class RequestDisplayQuestTilesSelection extends PCGEvent {
    public RequestDisplayQuestTilesSelection(List<TileTypes> t) {
        setPayload(t);
    }
}
