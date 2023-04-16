package util.eventrouting.events;

import util.eventrouting.PCGEvent;

public class RequestMatrixGeneratedRoom extends PCGEvent {
    private int[][] matrix;
    public RequestMatrixGeneratedRoom(int[][] matrix){
        this.matrix = matrix;
    }
    public int[][] getMatrix() {
        return matrix;
    }
}
