package util.eventrouting.events;

import game.Room;
import util.eventrouting.PCGEvent;

import java.util.ArrayList;

public class RequestScaleView extends PCGEvent {

    private ArrayList<Room> rooms;

    public RequestScaleView(ArrayList<Room> rooms){
        this.rooms = rooms;
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }
}
