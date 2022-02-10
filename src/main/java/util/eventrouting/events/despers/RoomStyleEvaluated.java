package util.eventrouting.events.despers;

import util.eventrouting.PCGEvent;

import java.util.UUID;

public class RoomStyleEvaluated extends PCGEvent {

    int room_style;
    UUID room_id;

    public RoomStyleEvaluated(int room_style, UUID room_id)
    {
        this.room_style = room_style;
        this.room_id = room_id;
//        setPayload(room_style);
    }
}
