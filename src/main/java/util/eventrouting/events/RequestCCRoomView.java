package util.eventrouting.events;

import game.MapContainer;
import util.eventrouting.PCGEvent;
/**
 * This event is used to request a view switch.
 *
 * @author Johan Holmberg, Malmö University
 * @author Chelsi Nolasco, Malmö University
 * @author Axel Österman, Malmö University
 * @author Tinea Larsson, Malmö University
 */
public class RequestCCRoomView extends PCGEvent{

    private int row;
    private int col;
    private MapContainer[][] matrix;

    public RequestCCRoomView(MapContainer payload, int row, int col, MapContainer[][] matrix) {
        this.row = row;
        this.col = col;
        this.matrix = matrix;
        System.out.println("INNE I REQUESTCCROOMVIEW");
        setPayload(payload);
    }

    public MapContainer[][] getMatrix() {
        return matrix;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }


}
