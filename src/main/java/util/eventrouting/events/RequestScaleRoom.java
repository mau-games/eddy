package util.eventrouting.events;

import game.Room;
import util.eventrouting.PCGEvent;

public class RequestScaleRoom extends PCGEvent {

    private int height;
    private int width;
    public RequestScaleRoom(Room scaleRoom, int height, int width){
        this.height = height;
        this.width = width;
        setPayload(scaleRoom);
    }
    public int getHeight(){
        return height;
    }
    public int getWidth(){
        return width;
    }
}
