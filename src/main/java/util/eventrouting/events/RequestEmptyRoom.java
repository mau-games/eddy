package util.eventrouting.events;

import game.MapContainer;
import util.eventrouting.PCGEvent;

/*
* @author Chelsi Nolasco, Malmö University
* @author Axel Österman, Malmö University
*/

public class RequestEmptyRoom extends PCGEvent {
	
	/**
	 * Creates a new event.
	 * 
	 * @param payload The map to be worked on.
	 */
	
	private int row;
	private int col;
	private MapContainer[][] matrix;
	
	public RequestEmptyRoom(MapContainer payload, int row, int col, MapContainer[][] matrix) {
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
