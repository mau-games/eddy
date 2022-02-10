package util.eventrouting.events.despers;

import util.eventrouting.PCGEvent;

import java.util.UUID;

public class BatchRoomStyleEvaluated extends PCGEvent
{
    int[] rooms_style;
    UUID sender_id;

    public BatchRoomStyleEvaluated(int[] rooms_style, UUID sender_id)
    {
        this.rooms_style = rooms_style;
        this.sender_id = sender_id;

//        this.room_id = room_id;
//        setPayload(room_style);
    }
}
