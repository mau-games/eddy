package util.eventrouting.events.despers;

import game.Room;
import util.eventrouting.PCGEvent;

public class DesPersEvaluation extends PCGEvent {

    public DesPersEvaluation(Room room_to_evaluate)
    {
        setPayload(room_to_evaluate);
    }

}
