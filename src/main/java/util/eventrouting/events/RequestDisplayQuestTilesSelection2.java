package util.eventrouting.events;

import java.util.List;

import game.TileTypes;
import util.eventrouting.PCGEvent;

public class RequestDisplayQuestTilesSelection2 extends PCGEvent{
	public RequestDisplayQuestTilesSelection2(List<TileTypes> t, finder.geometry.Point currentPoint) {
        setPayload(t);
        setPayload2(currentPoint);
    }
}
