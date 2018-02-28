package util.eventrouting.events;

import game.MapContainer;
import util.eventrouting.PCGEvent;

public class RequestSuggestionsView extends PCGEvent {
	private int row;
	private int col;
	private MapContainer[][] matrix;
	private int threads;


	public RequestSuggestionsView() {
		threads = 1;
	}

	public RequestSuggestionsView(MapContainer payload, int row, int col, MapContainer[][] matrix, int threads) {
		this.threads = threads;
		this.row = row;
		this.col = col;
		this.matrix = matrix;
		setPayload(payload);
	}

	public int getNbrOfThreads() {
		return threads;
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






