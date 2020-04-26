package util.eventrouting.events;

import game.Room;
import util.eventrouting.PCGEvent;

import java.util.Arrays;
import java.util.List;

public class RequestDisplayQuestTilePosition extends PCGEvent {
    private List<QuestPositionUpdate> pos;

    public RequestDisplayQuestTilePosition(QuestPositionUpdate... pos) {
        this.pos = Arrays.asList(pos);
    }

    public List<QuestPositionUpdate> getPos() {
        return pos;
    }
}
