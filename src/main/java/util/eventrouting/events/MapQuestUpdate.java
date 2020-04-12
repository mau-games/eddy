package util.eventrouting.events;

import game.Tile;
import util.eventrouting.PCGEvent;

public class MapQuestUpdate extends PCGEvent {
    private final Tile prev;
    private final Tile next;

    public MapQuestUpdate() {
        prev = null;
        next = null;
    }

    public MapQuestUpdate(Tile prev, Tile next){
        this.prev = prev;
        this.next = next;
    }

    public boolean hasPayload(){
        return prev != null && next != null;
    }

    public Tile getPrev() {
        return prev;
    }

    public Tile getNext() {
        return next;
    }
}
