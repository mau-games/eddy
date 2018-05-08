package util.eventrouting.events;

import game.MapContainer;
import util.eventrouting.PCGEvent;
/**
 * This event is used to request a view switch.
 * 
 * @author Johan Holmberg, Malmö University
 * @author Chelsi Nolasco, Malmö University
 * @author Axel Österman, Malmö University
 */
public class RequestRoomView extends PCGEvent{
	
	private int row;
	private int col;
	private MapContainer[][] matrix;
	
	public RequestRoomView(MapContainer payload, int row, int col, MapContainer[][] matrix) {
		this.row = row;
		this.col = col;
		this.matrix = matrix;
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
