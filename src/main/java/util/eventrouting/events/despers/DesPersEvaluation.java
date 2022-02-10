package util.eventrouting.events.despers;

import game.Room;
import util.eventrouting.PCGEvent;

import java.util.ArrayList;
import java.util.Arrays;

public class DesPersEvaluation extends PCGEvent {

    public ArrayList<Room> rooms = new ArrayList<Room>();

    public DesPersEvaluation(Room... room_to_evaluate)
    {
        this.rooms.clear();

        if(room_to_evaluate != null)
            rooms.addAll(Arrays.asList(room_to_evaluate));

        setPayload(room_to_evaluate);
    }

}
