package util.eventrouting.events;

import game.Room;
import game.Tile;
import util.eventrouting.PCGEvent;

import java.util.Random;

public class MapQuestUpdate extends PCGEvent {
    private final Tile prev;
    private final Tile next;
    private final Room room;

    public MapQuestUpdate() {
        prev = null;
        next = null;
        room = null;
    }

    public MapQuestUpdate(Tile prev, Tile next, Room room){
        this.prev = prev;
        this.next = next;
        this.room = room;
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

    public Room getRoom() {return room;}
}
