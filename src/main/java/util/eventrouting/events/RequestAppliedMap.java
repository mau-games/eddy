package util.eventrouting.events;

import game.Map;
import game.MapContainer;
import util.eventrouting.PCGEvent;

/* 
 *  @author Chelsi Nolasco, Malmö University
 * @author Axel Österman, Malmö University
*/

public class RequestAppliedMap extends PCGEvent{

	private int row;
	private int col;
	
	
	public RequestAppliedMap(Map map, int row, int col) {
		setCol(col);
		setRow(row);
		setPayload(map);
	}


	public int getRow() {
		return row;
	}


	public void setRow(int row) {
		this.row = row;
	}


	public int getCol() {
		return col;
	}


	public void setCol(int col) {
		this.col = col;
	}
}
