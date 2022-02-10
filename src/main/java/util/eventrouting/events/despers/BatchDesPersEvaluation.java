package util.eventrouting.events.despers;

import game.Room;
import util.eventrouting.PCGEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class BatchDesPersEvaluation extends PCGEvent {

    public ArrayList<Room> rooms = new ArrayList<Room>();
    public UUID requester_id;

    public BatchDesPersEvaluation( UUID unique_id, Room... room_to_evaluate)
    {
        this.rooms.clear();
        if(room_to_evaluate != null)
            this.rooms.addAll(Arrays.asList(room_to_evaluate));

        this.requester_id = unique_id;
    }

}
