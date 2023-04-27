package util.eventrouting.events;

import game.RoomScale;
import util.eventrouting.PCGEvent;

public class RequestMatrixScaledRoom extends PCGEvent {
    private int[][] matrix;
    private RoomScale roomScale;
    private boolean isEaScaled;
    public RequestMatrixScaledRoom(int[][] matrix){
        this.matrix = matrix;
    }
    public RequestMatrixScaledRoom(int[][] matrix, RoomScale roomScale, boolean isEaScaled){
        this.matrix = matrix;
        this.roomScale = roomScale;
        this.isEaScaled = isEaScaled;
    }
    public int[][] getMatrix() {
        return matrix;
    }
    public RoomScale getRoomScale(){return roomScale;}
    public boolean isEaScaled() {
        return isEaScaled;
    }
}
